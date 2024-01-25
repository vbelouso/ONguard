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
package com.redhat.ecosystemappeng.onguard.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.ecosystemappeng.onguard.model.Bulk;
import com.redhat.ecosystemappeng.onguard.service.nvd.NvdService;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.list.ListCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class LoadService {

    private static final String LOAD_RECORD = "load";
    private static final Logger LOGGER = LoggerFactory.getLogger(LoadService.class);

    private final ValueCommands<String, Bulk> bulkCommands;

    @Inject
    NvdService nvdService;

    @Inject
    VulnerabilityService vulnerabilityService;

    @ConfigProperty(name = "load.pageSize", defaultValue = "200")
    Integer pageSize;

    public LoadService(RedisDataSource ds) {
        this.bulkCommands = ds.value(Bulk.class);
    }

    public void loadFromNvdApi(LocalDateTime since) {
        try {
            Bulk bulk = bulkCommands.get(LOAD_RECORD);
            while (bulk == null || bulk.completed() == null) {
                if (bulk == null) {
                    bulk = Bulk.builder().started(LocalDateTime.now(ZoneId.systemDefault())).pageSize(pageSize).build();
                    LOGGER.info("Starting load of NVD database");
                    bulkCommands.set(LOAD_RECORD, bulk);
                }
                var vulnerabilities = nvdService.bulkLoad(bulk.index(), pageSize, since);
                var loaded = vulnerabilities.size();
                LOGGER.info("Loaded {} elements from NVD", loaded);
                Multi.createFrom().items(vulnerabilities.stream()).runSubscriptionOn(Infrastructure.getDefaultExecutor()).subscribe().with(vulnerabilityService::ingestNvdVulnerability);
                vulnerabilities.stream().parallel().forEach(vulnerabilityService::ingestNvdVulnerability);
                
                synchronized(this) {
                    bulk = bulkCommands.get(LOAD_RECORD);
                    var builder = Bulk.builder(bulk).index(bulk.index() + loaded).pageSize(pageSize);
                    if (loaded < pageSize) {
                        var completed = LocalDateTime.now(ZoneId.systemDefault());
                        builder.completed(completed);
                        
                    }
                    bulkCommands.set(LOAD_RECORD, builder.build());
                }
            }
            LOGGER.info("Load completed of {} elements", bulk.index());
        } catch (Exception e) {
            LOGGER.error("Unable to complete Load", e);
        }
    }

    public Bulk get() {
        return bulkCommands.get(LOAD_RECORD);
    }

    public synchronized Bulk cancel() {
        var bulk = bulkCommands.get(LOAD_RECORD);
        if(bulk == null) {
            return null;
        }
        bulk = Bulk.builder(bulk).completed(LocalDateTime.now(ZoneId.systemDefault())).build();
        return bulkCommands.setGet(LOAD_RECORD, bulk);
    }

    public void restart() {
        bulkCommands.getdel(LOAD_RECORD);
        loadFromNvdApi(null);
    }

    @Scheduled(cron = "{load.sync.cron}")
    public void sync() {
        var when = bulkCommands.get(LOAD_RECORD);
        if(when == null) {
            LOGGER.info("Skipping scheduled sync because the database has not been pre-loaded");
        } if (when.completed() == null) {
            LOGGER.info("Waiting for current migration to complete");
        } else {
            LOGGER.info("Started scheduled sync since: {}", when.completed());
            when = bulkCommands.getdel(LOAD_RECORD);
            loadFromNvdApi(when.completed());
        }
    }
}
