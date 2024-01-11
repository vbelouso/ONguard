package com.redhat.ecosystemappeng.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.junit.jupiter.api.Test;

import com.redhat.ecosystemappeng.service.nvd.NvdApi;

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
