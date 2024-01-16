package com.redhat.ecosystemappeng.model.nvd;

import java.util.Date;
import java.util.List;

public record NvdResponse(Integer resultsPerPage, Integer startIndex, Integer totalResults, String format, String version, Date timestamp, List<NvdVulnerability> vulnerabilities) {
    
}
