package com.redhat.ecosystemappeng.rest;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.redhat.ecosystemappeng.model.PurlsRequest;
import com.redhat.ecosystemappeng.model.Vulnerability;
import com.redhat.ecosystemappeng.service.VulnerabilityService;

import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("/purls")
public class PurlEndpoint {

    @Inject
    VulnerabilityService svc;

    @POST
    public Map<String, List<Vulnerability>> find(PurlsRequest request) {
        if(request == null || request.purls() == null || request.purls().isEmpty()) {
            return Collections.emptyMap();
        }
        return svc.findByPurls(request.purls());
    }
}
