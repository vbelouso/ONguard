package com.redhat.ecosystemappeng.model;

import java.util.Date;

import org.bson.Document;
import org.bson.codecs.pojo.annotations.BsonProperty;

import io.quarkus.mongodb.panache.common.MongoEntity;

@MongoEntity(collection = "cves", database = "exhort")
public record Cve(
        @BsonProperty("_id") String id,
        Date created,
        Date lastModified,
        Document cve) {

    public static class Builder {
        String id;
        Date created;
        Date lastModified;
        Document cve;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder created(Date created) {
            this.created = created;
            return this;
        }

        public Builder lastModified(Date lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        public Builder cve(Document cve) {
            this.cve = cve;
            return this;
        }

        public Cve build() {
            return new Cve(id, created, lastModified, cve);
        }

    }
}
