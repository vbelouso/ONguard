package com.redhat.ecosystemappeng.rest;

import java.util.List;

import com.redhat.ecosystemappeng.model.Vulnerability;
import com.redhat.ecosystemappeng.service.VulnerabilityService;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

@Path("/cves")
public class CveEndpoint {

    @Inject
    VulnerabilityService svc;

    @POST
    public List<Vulnerability> find(List<String> cves, @QueryParam("reload") boolean reload) {
        return svc.findByCveId(cves, reload);
    }

    @GET
    @Path("/{cveId}")
    public Vulnerability get(@PathParam("cveId") String cveId, @QueryParam("reload") boolean reload) {
        return svc.getByCveId(cveId, reload);
    }
}
