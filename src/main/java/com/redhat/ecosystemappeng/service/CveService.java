package com.redhat.ecosystemappeng.service;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.Document;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.ecosystemappeng.model.Cve;
import com.redhat.ecosystemappeng.model.CveAliasResponse;
import com.redhat.ecosystemappeng.model.IngestRecord;
import com.redhat.ecosystemappeng.service.nvd.NvdService;
import com.redhat.ecosystemappeng.service.osv.OsvGcsLoader;
import com.redhat.ecosystemappeng.service.osv.OsvLoader;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CveService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CveService.class);

    @Inject
    OsvLoader osvLoader;

    @Inject
    CveRepository cveRepository;

    @Inject
    NvdService nvdService;

    @Inject
    ObjectMapper mapper;

    @Inject
    IngestionRepository ingRepo;

    @ConfigProperty(name = "migration.osv.pageSize", defaultValue = "100")
    Integer pageSize;

    public List<Cve> find(List<String> vulnerabilities, boolean reload) {
        Map<String, Cve> existing;
        if (!reload) {
            existing = cveRepository.list("id in ?1", vulnerabilities).stream()
                    .collect(Collectors.toMap(Cve::id, cve -> cve));
        } else {
            existing = Collections.emptyMap();
        }
        return vulnerabilities.stream().parallel().map(id -> {
            if (existing.containsKey(id)) {
                return existing.get(id);
            } else {
                return load(osvLoader.getCveAlias(id));
            }
        }).toList();
    }

    public Cve get(String vulnerabilityId, boolean reload) {
        Cve existing = null;
        if (!reload) {
            existing = cveRepository.findById(vulnerabilityId);
        }
        if (existing != null) {
            return existing;
        }
        return load(osvLoader.getCveAlias(vulnerabilityId));
    }

    public Cve load(CveAliasResponse response) {
        if (response.cve() == null) {
            var cve = new Cve.Builder().id(response.vulnId()).created(new Date(System.currentTimeMillis())).build();
            cveRepository.persistOrUpdate(cve);
            return cve;
        }
        var data = nvdService.findByCve(response.cve());
        try {
            var cveBuilder = new Cve.Builder().id(response.vulnId()).created(new Date(System.currentTimeMillis()));
            if(data != null) {
                cveBuilder.cve(mapper.readValue(data, Document.class));
            }
            var cve = cveBuilder.build();
            cveRepository.persistOrUpdate(cve);
            return cve;
        } catch (IOException e) {
            LOGGER.error("Unable to decode cve data for {}", response.vulnId(), e);
            return new Cve.Builder().id(response.vulnId()).build();
        }
    }

    public void purge(List<String> cves) {
        cveRepository.deleteByCves(cves);
    }

    public void loadAll(boolean force) {
        OsvGcsLoader.PROVIDERS.stream().parallel().forEach(provider -> {
            loadProvider(provider, force);
        });
    }

    private void loadProvider(String provider, boolean force) {
        LOGGER.info("Loading {}", provider);
        try {
            var record = force ? null : ingRepo.findLatest(provider);
            String lastToken = null;
            if (record != null) {
                if (record.getFinished() != null) {
                    LOGGER.info("Skip sync for {}. Completed on {}", provider, record.getFinished());
                    return;
                }
                if (record.getLastPageToken() != null) {
                    LOGGER.info("Resuming sync for {}. Starting from {}", provider, record.getLastPageToken());
                    lastToken = record.getLastPageToken();
                }
            } else {
                record = new IngestRecord.Builder().folder(provider).build();
                ingRepo.persist(record);
                LOGGER.info("Starting sync for {}. No previous load found", provider, record.getLastPageToken());
            }
            var page = osvLoader.loadPage(provider, lastToken, pageSize).toList();
            while(page.size() > 0) {
                page.forEach(this::load);
                lastToken = String.format("%s/%s.json", provider, page.get(page.size() - 1).vulnId());
                record.setProcessed(record.getProcessed() + page.size())
                        .setLastPageToken(lastToken);
                ingRepo.update(record);
                page = osvLoader.loadPage(provider, lastToken, pageSize).toList();
                LOGGER.info("Loaded {} elements for {}", page.size(), provider);
            }
            LOGGER.info("Loaded {}", provider);
            record.setFinished(new Date(System.currentTimeMillis()));
            ingRepo.update(record);

        } catch (Exception e) {
            LOGGER.error("Unable to process provider {}", provider, e);
        }
    }

    public void deleteAll() {
        ingRepo.deleteAll();
        cveRepository.deleteAll();
    }

}
