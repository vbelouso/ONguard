package com.redhat.ecosystemappeng.service.nvd;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.ecosystemappeng.model.nvd.Metrics;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class NvdServiceImpl implements NvdService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NvdServiceImpl.class);
    
    @RestClient
    NvdApi nvdApi;

    @Override
    public Metrics getCveMetrics(String cveId) {
        try {
            var response = nvdApi.getCve(cveId);
            if (response.totalResults() == 0 || response.vulnerabilities() == null
                    || response.vulnerabilities().isEmpty()) {
                LOGGER.warn("Not found vulnerabilities for CVE: {} in NVD", cveId);
                return null;
            }
            if (response.vulnerabilities().size() == 1) {
                var cve = response.vulnerabilities().get(0).cve();
                return cve.metrics();
            } else {
                LOGGER.warn("Found multiple vulnerabilities for the same CVE: {}, found {}", cveId,
                        response.vulnerabilities().size());
            }
        } catch (ClientWebApplicationException e) {
            if (e.getResponse() != null && e.getResponse().getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                LOGGER.info("Not found vulnerability: {} in NVD", cveId);
            } else {
                LOGGER.error("Error retrieving NVD vulnerability for {}", cveId, e);
            }
        }
        return null;
    }

}
