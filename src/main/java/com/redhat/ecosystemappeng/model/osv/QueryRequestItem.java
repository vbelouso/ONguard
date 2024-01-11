package com.redhat.ecosystemappeng.model.osv;

import com.fasterxml.jackson.annotation.JsonProperty;

public record QueryRequestItem(@JsonProperty("package") PackageRef pkgRef) {

}
