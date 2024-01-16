package com.redhat.ecosystemappeng.service.nvd;

import java.time.temporal.ChronoUnit;

import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;

import com.redhat.ecosystemappeng.model.nvd.NvdResponse;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;

@Path("/rest/json/cves/2.0")
@RegisterRestClient(configKey = "nvd-api")
public interface NvdApi {

    static final long NVD_API_WINDOW_SECS = 30;

    @GET
    @ClientHeaderParam(name = "apiKey", value = "${migration.nvd.apikey}")
    @Produces(MediaType.APPLICATION_JSON)
    @Fallback(value = NvdFallbackService.class, applyOn = WebApplicationException.class, skipOn = ClientWebApplicationException.class)
    @CircuitBreaker(delay = NVD_API_WINDOW_SECS, delayUnit = ChronoUnit.SECONDS)
    NvdResponse getCve(@QueryParam("cveId") String cveId);

}
