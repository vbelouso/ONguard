package com.redhat.ecosystemappeng.service.nvd;

import java.time.Duration;
import java.util.Collections;
import java.util.Date;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.faulttolerance.ExecutionContext;
import org.eclipse.microprofile.faulttolerance.FallbackHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.ecosystemappeng.model.Vulnerability;
import com.redhat.ecosystemappeng.model.nvd.NvdResponse;
import com.redhat.ecosystemappeng.service.VulnerabilityRepository;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class NvdFallbackService implements FallbackHandler<NvdResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NvdFallbackService.class);

    @Inject
    VulnerabilityRepository repository;

    @Inject
    ManagedExecutor executor;

    @Inject
    NvdService nvdService;

    @ConfigProperty(name = "CircuitBreaker/delay", defaultValue = "30")
    Long delay;

    private void updateMetrics(String cveId) {
        var metrics = nvdService.getCveMetrics(cveId);
        if (metrics == null) {
            LOGGER.debug("Unable to retrieve metrics from NVD for CVE {}", cveId);
            return;
        }
        var vuln = repository.get(cveId);
        if (vuln != null) {
            var newVuln = new Vulnerability.Builder(vuln).metrics(metrics).lastModified(new Date()).build();
            repository.save(newVuln);
        }
    }

    @Override
    public NvdResponse handle(ExecutionContext context) {
        Uni.createFrom()
                .item((String) context.getParameters()[0])
                .onItem().delayIt().by(Duration.ofSeconds(delay)).invoke(cveId -> updateMetrics(cveId))
                .runSubscriptionOn(executor).subscribeAsCompletionStage();
        return new NvdResponse(0, 0, 0, null, null, new Date(), Collections.emptyList());
    }
}
