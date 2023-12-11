package com.redhat.ecosystemappeng.service;

import java.util.List;
import com.redhat.ecosystemappeng.model.Cve;

import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CveRepository implements PanacheMongoRepositoryBase<Cve, String> {
    
    public void deleteByCves(List<String> cves) {
        delete("cve.cveMetadata.cveId in ?1", cves);
    }
}

