package com.redhat.ecosystemappeng.service;

import com.redhat.ecosystemappeng.model.Cve;

import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CveRepository implements PanacheMongoRepositoryBase<Cve, String> {
    
}

