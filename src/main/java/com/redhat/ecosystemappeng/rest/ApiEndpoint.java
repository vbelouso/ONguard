package com.redhat.ecosystemappeng.rest;

import java.util.List;

import org.eclipse.microprofile.context.ManagedExecutor;

import com.redhat.ecosystemappeng.service.CveService;

import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

@Path("/")
public class ApiEndpoint {

    @Inject
    ManagedExecutor executor;

    @Inject
    CveService cveService;

    @POST
    @Path("/cves")
    public Response findByIds(List<String> vulnerabilities, @QueryParam("reload") boolean reload) {
        return Response.ok(cveService.find(vulnerabilities, reload)).build();
    }

    @GET
    @Path("/sync")
    public Response loadAll(@QueryParam("force") boolean force) {
        executor.runAsync(() -> cveService.loadAll(force));
        return Response.accepted().build();
    }

    @DELETE
    @Path("/sync")
    public Response deleteAll() {
        executor.runAsync(() -> cveService.deleteAll());
        return Response.accepted().build();
    }

}
