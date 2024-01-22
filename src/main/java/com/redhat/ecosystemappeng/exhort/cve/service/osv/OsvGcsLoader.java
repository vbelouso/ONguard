/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.ecosystemappeng.exhort.cve.service.osv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.redhat.ecosystemappeng.exhort.cve.model.CveAliasResponse;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class OsvGcsLoader implements OsvLoader {

  private static final Logger LOGGER = LoggerFactory.getLogger(OsvGcsLoader.class);

  private static final String OSV_BUCKET = "osv-vulnerabilities";

  Storage storage;

  @Inject
  ObjectMapper mapper;

  @PostConstruct
  public void init() {
    storage = StorageOptions.getUnauthenticatedInstance().getService();
  }

  @Override
  public List<CveAliasResponse> fetch(List<String> aliases) {
    return aliases.stream().parallel().map(this::getCveByAlias).toList();
  }

  @Override
  public CveAliasResponse getCveByAlias(String alias) {
    var results = storage.list(OSV_BUCKET, Storage.BlobListOption.matchGlob(String.format("**/%s.json", alias)));
    var found = results.streamAll().map(blob -> toAlias(alias, blob)).findFirst();
    if (found.isPresent()) {
      return found.get();
    }
    return new CveAliasResponse(null, List.of(alias), null);
  }

  private CveAliasResponse toAlias(String vulnId, Blob blob) {
    try {
      JsonNode content = mapper.readTree(blob.getContent());
      var elements = content.withArray("aliases").elements();
      List<String> aliases = new ArrayList<>();
      String cveId = null;
      while (elements.hasNext()) {
        String alias = elements.next().asText();
        if (alias.startsWith("CVE-")) {
          cveId = alias;
        } else {
          aliases.add(alias);
        }
      }
      return new CveAliasResponse(cveId, aliases, null);
    } catch (IOException e) {
      LOGGER.error("Unable to parse content for {}", blob.getName(), e);
      return new CveAliasResponse(null, List.of(vulnId), e.getMessage());
    }
  }

  @Override
  public Stream<CveAliasResponse> loadPage(String provider, String lastToken, long pageSize) {
    var folder = provider + "/";
    try {
      var options = defaultOptions(folder, pageSize);
      if (lastToken != null) {
        options.add(Storage.BlobListOption.endOffset(lastToken));
      }
      return storage.list(OSV_BUCKET,
          options.toArray(new Storage.BlobListOption[0])).streamValues().map(this::toCveAlias);
    } catch (Exception e) {
      LOGGER.error("Unable to process folder {}", folder, e);
      return Stream.empty();
    }
  }

  private CveAliasResponse toCveAlias(Blob blob) {
    try {
      var vuln = mapper.readTree(blob.getContent());
      var id = vuln.get("id").asText();
      return toAlias(id, blob);
    } catch (IOException e) {
      LOGGER.error("Unable to parse blob {}", blob.getName(), e);
      return new CveAliasResponse(null, null, e.getMessage());
    }
  }

  private List<Storage.BlobListOption> defaultOptions(String folder, long pageSize) {
    List<Storage.BlobListOption> opts = new ArrayList<>();
    opts.add(Storage.BlobListOption.prefix(folder));
    opts.add(Storage.BlobListOption.currentDirectory());
    opts.add(Storage.BlobListOption.matchGlob("**.json"));
    opts.add(Storage.BlobListOption.pageSize(pageSize));
    return opts;
  }

}