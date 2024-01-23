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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.junit.jupiter.api.Test;

import com.redhat.ecosystemappeng.onguard.service.nvd.NvdApi;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(WireMockExtensions.class)
class NvdApiTest {
    
    private static final String NOT_FOUND = "not_found";

    @RestClient
    NvdApi nvdApi;

    @Test
    void testGetFound() {
        assertNotNull(nvdApi.getCve(WireMockExtensions.VALID_CVE));
    }

    @Test
    void testGetNotFound() {
        assertThrows(ClientWebApplicationException.class, () -> nvdApi.getCve(NOT_FOUND));
    }
}
