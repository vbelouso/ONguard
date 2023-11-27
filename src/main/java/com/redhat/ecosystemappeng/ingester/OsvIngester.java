package com.redhat.ecosystemappeng.ingester;

import static com.redhat.ecosystemappeng.service.osv.VulnerabilityLoader.PROVIDERS;

import java.util.List;

import org.eclipse.microprofile.context.ManagedExecutor;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.ecosystemappeng.model.Vulnerability;
import com.redhat.ecosystemappeng.service.nvd.NvdFileService;
import com.redhat.ecosystemappeng.service.osv.IngestionRepository;
import com.redhat.ecosystemappeng.service.osv.VulnerabilityLoader;
import com.redhat.ecosystemappeng.service.osv.VulnerabilityRepository;
import com.redhat.ecosystemappeng.service.osv.VulnerabilityRepository.VulnerabilityId;

import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class OsvIngester {

    @Inject
    VulnerabilityLoader loader;

    @Inject
    VulnerabilityRepository repository;

    @Inject
    IngestionRepository ingestions;

    @Inject ManagedExecutor executor;

    @Inject
    NvdFileService nvdService;

    // @Scheduled(every="1h")
    @GET
    @Path("/sync")
    public Response sync(@QueryParam("force") boolean force) {
        executor.submit(() -> loader.load(force));
        return Response.accepted().build();
    }
    
    @GET
    @Path("/sync/{provider}")
    public Response syncProvider(@PathParam("provider") String provider, @QueryParam("force") boolean force) {
        if (!PROVIDERS.contains(provider)) {
            return Response.status(Status.NOT_ACCEPTABLE).entity("Invalid provider " + provider + ". Expecting one of: " + PROVIDERS).build();
        } 
        executor.submit(() -> loader.load(provider, force));
        return Response.accepted().build();
    }

    @GET
    @Path("/vulnerabilities")
    public List<VulnerabilityId> list() {
        return repository.findAllIds();
    }

    @GET
    @Path("/vulnerabilities/{vulnId}")
    public Vulnerability find(@PathParam("vulnId") String vulnId) {
        return repository.findById(vulnId);
    }

    @DELETE
    @Path("/vulnerabilities")
    public void delete() {
        repository.deleteAll();
        ingestions.deleteAll();
    }

    @GET
    @Path("/cve/{cveId}")
    public JsonNode getCve(@PathParam("cveId") String cveId) {
        return nvdService.findByCve(cveId);
    }

    @GET
    @Path("/purl")
    public List<Vulnerability> findByPurl(@QueryParam("purl") String purl) {
        return repository.findByPurl(purl);
    }
    
}
