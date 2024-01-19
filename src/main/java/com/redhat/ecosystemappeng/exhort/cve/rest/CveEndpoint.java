/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.ecosystemappeng.exhort.cve.rest;

import java.util.List;

import com.redhat.ecosystemappeng.exhort.cve.model.Vulnerability;
import com.redhat.ecosystemappeng.exhort.cve.service.VulnerabilityService;

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
