package com.redhat.ecosystemappeng.model.nvd;

import java.util.List;

public record Metrics(List<Cvss_V31> cvssMetricV31, List<Cvss_V30> cvssMetricV30, List<Cvss_V2> cvssMetricV2) {
    
}
