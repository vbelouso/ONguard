package com.redhat.ecosystemappeng.model;

import java.util.Date;

public class IngestRecord {

    Long id;
    Date started;
    Date finished;
    Long processed;
    String folder;
    String lastPageToken;

    public IngestRecord() {
    }

    public IngestRecord(Long id,
            Date started,
            Date finished,
            Long processed,
            String folder,
            String lastPageToken) {
        this.id = id;
        this.started = started;
        this.finished = finished;
        this.processed = processed;
        this.folder = folder;
        this.lastPageToken = lastPageToken;
    }

    public IngestRecord setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getId() {
        return id;
    }

    public IngestRecord setStarted(Date started) {
        this.started = started;
        return this;
    }

    public Date getStarted() {
        return started;
    }

    public IngestRecord setFinished(Date finished) {
        this.finished = finished;
        return this;
    }

    public Date getFinished() {
        return finished;
    }

    public IngestRecord setProcessed(Long processed) {
        this.processed = processed;
        return this;
    }

    public Long getProcessed() {
        return processed;
    }

    public IngestRecord setFolder(String folder) {
        this.folder = folder;
        return this;
    }

    public String getFolder() {
        return folder;
    }

    public IngestRecord setLastPageToken(String lastPageToken) {
        this.lastPageToken = lastPageToken;
        return this;
    }

    public String getLastPageToken() {
        return lastPageToken;
    }

    public static class Builder {
        Long id;
        Date started;
        Date finished;
        Long processed;
        String folder;
        String lastPageToken;

        public Builder() {
        }

        public Builder(IngestRecord other) {
            this.id = other.id;
            this.started = other.started;
            this.finished = other.finished;
            this.processed = other.processed;
            this.folder = other.folder;
            this.lastPageToken = other.lastPageToken;
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder started(Date started) {
            this.started = started;
            return this;
        }

        public Builder finished(Date finished) {
            this.finished = finished;
            return this;
        }

        public Builder processed(Long processed) {
            this.processed = processed;
            return this;
        }

        public Builder folder(String folder) {
            this.folder = folder;
            return this;
        }

        public Builder lastPageToken(String lastPageToken) {
            this.lastPageToken = lastPageToken;
            return this;
        }

        public IngestRecord build() {
            if (started == null) {
                started = new Date(System.currentTimeMillis());
            }
            if (processed == null) {
                processed = 0l;
            }
            return new IngestRecord(id, started, finished, processed, folder, lastPageToken);
        }
    }
}
