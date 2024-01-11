package com.redhat.ecosystemappeng.model.nvd;

public record Cvss_V31(String source, String type, CvssData cvssData, Float exploitabilityScore, Float impactScore) {

    
}
