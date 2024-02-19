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

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.ecosystemappeng.onguard.model.Bulk;
import com.redhat.ecosystemappeng.onguard.model.Bulk.Status;
import com.redhat.ecosystemappeng.onguard.repository.BulkRepository;
import com.redhat.ecosystemappeng.onguard.service.nvd.NvdService;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class LoadService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadService.class);

    @Inject
    NvdService nvdService;

    @Inject
    VulnerabilityService vulnerabilityService;

    @Inject
    BulkRepository bulkRepository;

    @ConfigProperty(name = "load.pageSize", defaultValue = "1000")
    Integer pageSize;

    public void loadFromNvdApi(LocalDateTime since) {
        Bulk bulk = bulkRepository.get();
        if (bulk == null) {
            bulk = Bulk.builder().started(LocalDateTime.now(ZoneId.systemDefault())).pageSize(pageSize)
                    .status(Status.PROCESSING).build();
            LOGGER.info("Starting load of NVD database");
        } else {
            bulk = Bulk.builder(bulk).status(Status.PROCESSING).build();
            LOGGER.info("Resuming load of NVD database");
        }
        bulkRepository.set(bulk);
        try {
            while (Status.PROCESSING.equals(bulk.status())) {
                var vulnerabilities = nvdService.bulkLoad(bulk.index(), pageSize, since);
                var loaded = vulnerabilities.size();
                LOGGER.info("Loaded {} elements from NVD. Current load index: {}", loaded, bulk.index());
                Multi.createFrom().items(vulnerabilities.stream())
                        .runSubscriptionOn(Infrastructure.getDefaultExecutor()).subscribe()
                        .with(vulnerabilityService::ingestNvdVulnerability);
                vulnerabilities.stream().parallel().forEach(vulnerabilityService::ingestNvdVulnerability);

                synchronized (this) {
                    bulk = bulkRepository.get();
                    var builder = Bulk.builder(bulk).index(bulk.index() + loaded).pageSize(pageSize);
                    if (loaded < pageSize) {
                        var completed = LocalDateTime.now(ZoneId.systemDefault());
                        builder.completed(completed)
                                .status(Status.COMPLETED);

                    }
                    bulkRepository.set(builder.build());
                }
            }
            LOGGER.info("Load completed of {} elements", bulk.index());
        } catch (Throwable e) {
            LOGGER.error("Unable to complete Load", e);
            synchronized (this) {
                if (bulk != null) {
                    bulk = Bulk.builder(bulk)
                            .completed(LocalDateTime.now(ZoneId.systemDefault()))
                            .status(Status.COMPLETED_WITH_ERRORS)
                            .build();
                    bulkRepository.set(bulk);
                }
            }
        }
    }

    public Bulk get() {
        return bulkRepository.get();
    }

    public synchronized Bulk cancel() {
        var bulk = bulkRepository.get();
        if (bulk == null) {
            return null;
        }
        bulk = Bulk.builder(bulk).completed(LocalDateTime.now(ZoneId.systemDefault())).build();
        bulkRepository.set(bulk);
        return bulk;
    }

    public void restart() {
        bulkRepository.remove();
        loadFromNvdApi(null);
    }

}
