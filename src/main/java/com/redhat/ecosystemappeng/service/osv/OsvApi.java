package com.redhat.ecosystemappeng.service.osv;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.redhat.ecosystemappeng.model.osv.OsvVulnerability;
import com.redhat.ecosystemappeng.model.osv.QueryRequest;
import com.redhat.ecosystemappeng.model.osv.QueryResult;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@Path("/v1")
@RegisterRestClient(configKey = "osv-api")
public interface OsvApi {
    
    @GET
    @Path("/vulns/{vulnId}")
    OsvVulnerability getVuln(@PathParam("vulnId") String vulnId);

    @POST
    @Path("/querybatch")
    QueryResult queryBatch(QueryRequest request);

}
