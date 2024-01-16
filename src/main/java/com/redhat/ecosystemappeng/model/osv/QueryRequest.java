package com.redhat.ecosystemappeng.model.osv;

import java.util.List;

public record QueryRequest(List<QueryRequestItem> queries) {
    
}
