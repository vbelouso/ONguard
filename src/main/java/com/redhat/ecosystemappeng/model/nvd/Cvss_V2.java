package com.redhat.ecosystemappeng.model.nvd;

public record Cvss_V2(String source, String type, CvssData cvssData, String baseSeverity, Float exploitabilityScore,
        Float impactScore, Boolean acInsufInfo, Boolean obtainAllPrivilege, Boolean obtainUserPrivilege,
        Boolean obtainOtherPrivilege, Boolean userInteractionRequired) {

}
