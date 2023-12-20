package com.redhat.ecosystemappeng.model;

import java.util.List;

public record CveAliasResponse(String cveId, List<String> aliases, String error) {
    
}
