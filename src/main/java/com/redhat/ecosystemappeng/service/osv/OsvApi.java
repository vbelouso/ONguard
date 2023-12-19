package com.redhat.ecosystemappeng.service.osv;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.redhat.ecosystemappeng.model.OsvVulnerability;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@Path("/v1")
@RegisterRestClient(configKey = "osv-api")
public interface OsvApi {
    
    @GET
    @Path("/vulns/{vulnId}")
    OsvVulnerability getVuln(@PathParam("vulnId") String vulnId);

}
