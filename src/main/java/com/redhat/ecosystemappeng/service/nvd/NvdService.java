package com.redhat.ecosystemappeng.service.nvd;

import com.redhat.ecosystemappeng.model.nvd.Metrics;

public interface NvdService {

    Metrics getCveMetrics(String cveId);

}
