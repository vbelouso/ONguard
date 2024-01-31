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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.http.HttpHeader;

import com.github.tomakehurst.wiremock.WireMockServer;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import jakarta.ws.rs.core.MediaType;

public class WireMockExtensions implements QuarkusTestResourceLifecycleManager {

    public static final String NVD_API_KEY = "nvd-api-123";
    public static final String VALID_CVE = "CVE-2022-24684";

    private WireMockServer wireMockServer;

    @Override
    public Map<String, String> start() {
        wireMockServer = new WireMockServer();
        wireMockServer.start();

        wireMockServer.stubFor(get(urlPathEqualTo("/rest/json/cves/2.0"))
            .withHeader("apiKey", equalTo(NVD_API_KEY))
            .withQueryParam("cveId", equalTo(VALID_CVE ))
            .willReturn(aResponse().withStatus(200).withBodyFile("nvd-data/" + VALID_CVE + ".json")
            .withHeader(HttpHeader.CONTENT_TYPE.asString(), MediaType.APPLICATION_JSON)));
        
        Map<String, String> props = new HashMap<>();
        props.put("quarkus.rest-client.nvd-api.url", wireMockServer.baseUrl());
        props.put("api.nvd.apikey", NVD_API_KEY);
        return props;
    }

    @Override
    public void stop() {
        if(wireMockServer != null) {
            wireMockServer.stop();
        }
    }
    
}
