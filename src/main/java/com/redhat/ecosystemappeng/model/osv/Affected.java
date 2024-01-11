package com.redhat.ecosystemappeng.model.osv;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Affected(@JsonProperty("package") AffectedPackage affectedPackage, List<Range> ranges,
        List<String> versions) {

}
