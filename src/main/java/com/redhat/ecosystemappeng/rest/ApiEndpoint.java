package com.redhat.ecosystemappeng.rest;

import java.util.List;

import org.eclipse.microprofile.context.ManagedExecutor;

import com.redhat.ecosystemappeng.model.Cve;
import com.redhat.ecosystemappeng.service.CveService;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

@Path("/vulnerabilities")
public class ApiEndpoint {

    @Inject
    ManagedExecutor executor;

    @Inject
    CveService cveService;

    @POST
    @Path("/")
    public List<Cve> find(List<String> vulnerabilities, @QueryParam("reload") boolean reload) {
        return cveService.find(vulnerabilities, reload);
    }

    @GET
    @Path("/{vulnId}")
    public Cve get(String vulnId, @QueryParam("reload") boolean reload) {
        return cveService.get(vulnId, reload);
    }

}
