package com.redhat.ecosystemappeng.model.nvd;

public record Cvss_V30(String source, String type, CvssData cvssData, Float exploitabilityScore, Float impactScore) {

    
}
