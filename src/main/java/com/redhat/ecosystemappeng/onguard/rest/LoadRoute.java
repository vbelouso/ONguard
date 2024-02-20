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
package com.redhat.ecosystemappeng.onguard.rest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.ecosystemappeng.onguard.service.LoadService;

import io.quarkus.vertx.http.ManagementInterface;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@ApplicationScoped
public class LoadRoute {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadRoute.class);

    @Inject
    LoadService loadService;

    @Inject
    ObjectMapper mapper;

    public void registerManagementRoutes(@Observes ManagementInterface mi) {
        mi.router().post("/load").handler(ctx -> {
            final LocalDateTime since;
            final LocalDateTime reloadBefore;
            try {
                since = parseDateParam(ctx.request().getParam("since"));
                reloadBefore = parseDateParam(ctx.request().getParam("reload_before"));
            } catch (DateTimeParseException e) {
                ctx.response().setStatusCode(400).setStatusMessage(e.getMessage()).end();
                return;
            }
            Boolean reload = Boolean.TRUE.toString().equalsIgnoreCase(ctx.request().getParam("reload"));
            Uni.createFrom()
                    .voidItem()
                    .invoke(() -> {
                        if(reloadBefore != null) {
                          var bulk = loadService.get();
                          if(bulk != null && bulk.completed()!= null && reloadBefore.isBefore(bulk.completed())) {
                            loadService.restart();
                          }
                        } else if(reload) {
                            loadService.restart();
                        }
                        loadService.loadFromNvdApi(since);
                    })
                    .runSubscriptionOn(Infrastructure.getDefaultExecutor())
                    .subscribeAsCompletionStage()
                    .whenComplete((s, e) -> {
                        if (e != null) {
                            LOGGER.error("Load interrupted", e);
                        } else {
                            LOGGER.info("Completed");
                        }
                    });
            ctx.response().setStatusCode(202).end();
        });

        mi.router().get("/load").blockingHandler(ctx -> {
            try {
                var bulk = loadService.get();
                if (bulk == null) {
                    ctx.response().setStatusCode(404).end();
                } else {
                    ctx.response().headers().add("Content-Type", "application/json");
                    ctx.response().setStatusCode(200).end(mapper.writeValueAsString(bulk));
                }
            } catch (JsonProcessingException e) {
                ctx.response().setStatusCode(500).setStatusMessage(e.getMessage()).end();
            }
        });

        mi.router().delete("/load").blockingHandler(ctx -> {
            try {
                var bulk = loadService.cancel();
                if (bulk == null) {
                    ctx.response().setStatusCode(404).end();
                } else {
                    ctx.response().headers().add("Content-Type", "application/json");
                    ctx.response().setStatusCode(200).end(mapper.writeValueAsString(bulk));
                }
            } catch (JsonProcessingException e) {
                ctx.response().setStatusCode(500).setStatusMessage(e.getMessage()).end();
            }
        });
    }

    private LocalDateTime parseDateParam(String param) throws DateTimeParseException {
        if(param != null) {
           return LocalDateTime.parse(param, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        return null;
    }

}
