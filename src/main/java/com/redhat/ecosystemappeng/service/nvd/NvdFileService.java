package com.redhat.ecosystemappeng.service.nvd;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NvdFileService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(NvdFileService.class);
    
    @ConfigProperty(name = "migration.nvd.file.path") Path repositoryPath;

    public byte[] findByCve(String cve) {
        String year = cve.replace("CVE-", "");
        year = year.substring(0, year.indexOf("-"));
        var path = repositoryPath.resolve(year);
        if(!Files.exists(path)) {
            return null;
        }
        try(Stream<Path> walkStream = Files.walk(path)) {
            var match = walkStream.filter(Files::isRegularFile).filter(f -> f.getFileName().toString().equals(cve + ".json")).findFirst();
            if(match.isPresent()) {
                return Files.readAllBytes(match.get());
            }
        } catch (IOException e) {
            LOGGER.warn("Unable to parse cve: ", cve, e);
        }
        return null;
    }

}
