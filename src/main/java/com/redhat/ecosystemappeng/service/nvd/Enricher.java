package com.redhat.ecosystemappeng.service.nvd;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.ecosystemappeng.model.Vulnerability;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class Enricher {

    private static final Logger LOGGER = LoggerFactory.getLogger(Enricher.class);

    @Inject
    NvdFileService nvdService;

    public Vulnerability enrich(Vulnerability vulnerability) {
        if (vulnerability.cve() == null) {
            return vulnerability;
        }
        var cveNode = nvdService.findByCve(vulnerability.cve());
        if(cveNode == null) {
            LOGGER.warn("CVE {} not found in NVD database", vulnerability.cve());
            return vulnerability;
        }
        return new Vulnerability.Builder()
                .vulnId(vulnerability.vulnId())
                .cve(vulnerability.cve())
                .published(vulnerability.published())
                .modified(vulnerability.modified())
                .osvRaw(vulnerability.osvRaw())
                .nvdRaw(Document.parse(cveNode.toPrettyString()))
                .build();
    }

}
