package com.redhat.ecosystemappeng.service;

import com.redhat.ecosystemappeng.model.IngestRecord;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class IngestionRepository implements PanacheMongoRepository<IngestRecord> {

    public IngestRecord findLatest(String folder) {
        return find("folder", Sort.descending("started"), folder).firstResult();
    }
}
