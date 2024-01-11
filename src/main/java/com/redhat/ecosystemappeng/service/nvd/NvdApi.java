package com.redhat.ecosystemappeng.service.nvd;

import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.redhat.ecosystemappeng.model.nvd.NvdResponse;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/rest/json/cves/2.0")
@RegisterRestClient(configKey="nvd-api")
public interface NvdApi {
    
    @GET
    @ClientHeaderParam(name = "apiKey", value = "${migration.nvd.apikey}")
    @Produces(MediaType.APPLICATION_JSON)
    NvdResponse getCve(@QueryParam("cveId") String cveId);

}
