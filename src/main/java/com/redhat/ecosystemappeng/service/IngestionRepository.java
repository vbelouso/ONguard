package com.redhat.ecosystemappeng.service;

import com.redhat.ecosystemappeng.model.IngestRecord;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class IngestionRepository {

    @Inject
    RedisDataSource rds;

    private ValueCommands<String, IngestRecord> valueCommands;
    
    public IngestionRepository(RedisDataSource ds) {
        valueCommands = ds.value(IngestRecord.class);
    }

    public void save(IngestRecord item) {
        // valueCommands.set(item.getFolder(), item);
    }

    public IngestRecord get(String folder) {
        // return valueCommands.get(folder);
        return null;
    }

    public void delete(String folder) {
        // valueCommands.getdel(folder);
    }

}
