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
package com.redhat.ecosystemappeng.onguard.model;

import java.time.LocalDateTime;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record Bulk(LocalDateTime started, LocalDateTime completed, Integer index, Integer pageSize, Status status) {
    
    public static enum Status {
        COMPLETED,
        PROCESSING,
        COMPLETED_WITH_ERRORS
    }

    public static class Builder {
        LocalDateTime started;
        LocalDateTime completed;
        Integer index;
        Integer pageSize;
        Status status;


        private Builder() {}

        private Builder(Bulk other) {
            this.started = other.started;
            this.completed = other.completed;
            this.index = other.index;
            this.pageSize = other.pageSize;
            this.status = other.status;
        }

        public Builder started(LocalDateTime started) {
            this.started = started;
            return this;
        }
        public Builder completed(LocalDateTime completed) {
            this.completed = completed;
            return this;
        }
        public Builder index(Integer index) {
            this.index = index;
            return this;
        }
        public Builder pageSize(Integer pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public Builder status(Status status) {
            this.status = status;
            return this;
        }

        public Bulk build() {
            if(index == null) {
                index = 0;
            }
            return new Bulk(started, completed, index, pageSize, status);
        }
    }

    public static Bulk.Builder builder(Bulk other) {
        return new Builder(other);
    }

    public static Bulk.Builder builder() {
        return new Builder();
    }
}