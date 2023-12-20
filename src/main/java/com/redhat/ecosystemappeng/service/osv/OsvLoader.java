package com.redhat.ecosystemappeng.service.osv;

import java.util.List;
import java.util.stream.Stream;

import com.redhat.ecosystemappeng.model.CveAliasResponse;

public interface OsvLoader {
    List<CveAliasResponse> fetch(List<String> aliases);

    CveAliasResponse getCveByAlias(String alias);

    Stream<CveAliasResponse> loadPage(String provider, String lastToken, long pageSize);

}
