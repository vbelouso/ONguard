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
package com.redhat.ecosystemappeng.onguard.service.cve;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CveFileService implements CveService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CveFileService.class);
    public static final Pattern CVE_PATTERN = Pattern.compile("CVE-\\d{4}-\\d{4,7}", Pattern.CASE_INSENSITIVE);

    @ConfigProperty(name = "migration.cve.file.path")
    Path repositoryPath;

    @Override
    public byte[] findByCve(String cve) {
        if(cve == null || cve.isEmpty()) {
            return null;
        }
        if(!CVE_PATTERN.matcher(cve).matches()) {
            return null;
        }
        String year = cve.replace("CVE-", "");
        year = year.substring(0, year.indexOf("-"));
        var path = repositoryPath.resolve(year);
        if (!Files.exists(path)) {
            return null;
        }
        try (Stream<Path> walkStream = Files.walk(path)) {
            var match = walkStream.filter(Files::isRegularFile)
                    .filter(f -> f.getFileName().toString().equals(cve + ".json")).findFirst();
            if (match.isPresent()) {
                return Files.readAllBytes(match.get());
            }
        } catch (IOException e) {
            LOGGER.warn("Unable to parse cve: ", cve, e);
        }
        return null;
    }

}
