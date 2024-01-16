package com.redhat.ecosystemappeng.model.nvd;

import java.util.Date;

public record NvdCve(String id, Metrics metrics, String vulnStatus, Date publisihed, Date lastModified) {
    
}
