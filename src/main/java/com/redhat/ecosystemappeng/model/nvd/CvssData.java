package com.redhat.ecosystemappeng.model.nvd;

public record CvssData(String version, String vectorString, String attackVector, String attackComplexity,
        String privilegesRequired, String userInteraction, String scope, String confidentialityImpact,
        String integrityImpact, String availabilityImpact, Float baseScore, String baseSeverity) {

}
