/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.ecosystemappeng.onguard.service;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import com.redhat.ecosystemappeng.onguard.model.Vulnerability;
import com.redhat.ecosystemappeng.onguard.model.nvd.Cvss_V31;
import com.redhat.ecosystemappeng.onguard.model.nvd.Metrics;
import com.redhat.ecosystemappeng.onguard.model.nvd.NvdCve;
import com.redhat.ecosystemappeng.onguard.model.nvd.NvdResponse;
import com.redhat.ecosystemappeng.onguard.model.nvd.NvdVulnerability;
import com.redhat.ecosystemappeng.onguard.repository.VulnerabilityRepository;
import com.redhat.ecosystemappeng.onguard.service.nvd.NvdService;
import com.redhat.ecosystemappeng.onguard.service.nvd.NvdApi;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;

@QuarkusTest
@TestProfile(NvdServiceTest.NvdServiceTestProfile.class)
public class NvdServiceTest {

    private static final String VALID_CVE = "cve-2012-33030";
    
    @Inject
    NvdService nvdService;

    @InjectMock
    @RestClient
    NvdApi nvdApi;

    @InjectMock
    VulnerabilityRepository repository;

    @Test
    void testRatioExceeded() throws InterruptedException {
        when(nvdApi.get(eq(VALID_CVE)))
            .thenThrow(new WebApplicationException(403))
            .thenReturn(validResponse());
        when(repository.get(eq(VALID_CVE))).thenReturn(new Vulnerability(Collections.emptyList(), VALID_CVE, new Date(), null, null, null, null, null));

        var result = nvdService.getCveMetrics(VALID_CVE);
        assertNull(result);

        Thread.sleep(1500L);

        verify(nvdApi, times(2)).get(eq(VALID_CVE));
        verify(repository, times(1)).get(eq(VALID_CVE));
        verify(repository, times(1)).save(Mockito.argThat(new ArgumentMatcher<Vulnerability>() {

            @Override
            public boolean matches(Vulnerability vuln) {
                return vuln.cveId().equals(VALID_CVE) && vuln.metrics() != null;
            }
            
        }));
    }

    @Test
    void testNotFound() throws InterruptedException {
        when(nvdApi.get(eq(VALID_CVE)))
            .thenThrow(new ClientWebApplicationException(404));

        var result = nvdService.getCveMetrics(VALID_CVE);
        assertNull(result);

        Thread.sleep(1500L);

        verify(nvdApi, times(1)).get(eq(VALID_CVE));
        verifyNoInteractions(repository);
    }

    public static final class NvdServiceTestProfile implements QuarkusTestProfile {

        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("CircuitBreaker/delay", "1");
        }
    }

    private NvdResponse validResponse() {
        var metric = new Cvss_V31("nvd@dist.gov", "Primary", null, 3f, 2f);
        var metrics = new Metrics(List.of(metric),null, null);
        var vuln = new NvdVulnerability(new NvdCve(VALID_CVE, metrics, "Analyized", null, null));
        return new NvdResponse(1, 0, 1, "NVD_CVE", "2.0", new Date(), List.of(vuln));
    }
    
}
