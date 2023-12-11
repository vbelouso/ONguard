package com.redhat.ecosystemappeng.rest;

import java.util.List;
import java.util.concurrent.ExecutorService;

import com.redhat.ecosystemappeng.service.CveService;

import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/cves")
public class CveEndpoint {

    @Inject
    ExecutorService executor;

    @Inject
    CveService cveService;

    @POST
    public Response purge(List<String> cves) {
        executor.submit(() -> cveService.purge(cves));
        return Response.accepted().build();
    }

}
