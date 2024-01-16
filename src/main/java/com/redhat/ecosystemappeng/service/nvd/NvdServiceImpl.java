package com.redhat.ecosystemappeng.service.nvd;

import org.eclipse.microprofile.faulttolerance.exceptions.CircuitBreakerOpenException;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.ecosystemappeng.model.nvd.Metrics;

import jakarta.enterprise.context.ApplicationScoped;

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
            if (e.getResponse() != null) {
                switch (e.getResponse().getStatus()) {
                    case 404:
                        LOGGER.info("Not found vulnerability: {} in NVD", cveId);
                        break;
                    case 403:
                        LOGGER.info(
                                "Unable to retrieve vulnerability: {} from NVD. Rate limit reached. Wait for async loading or retry after 30 seconds",
                                cveId);
                        break;
                    default:
                        LOGGER.error("Error retrieving NVD vulnerability for {}", cveId, e);
                        break;
                }
            } else {
                LOGGER.error("Error retrieving NVD vulnerability for {}", cveId, e);
            }
        } catch (CircuitBreakerOpenException e) {
            LOGGER.info("Waiting for the circuit breaker to close. CVE: {}", cveId, e.getMessage());
        }
        return null;
    }

}
