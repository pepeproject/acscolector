

package com.globo.pepe.acscollector.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SuppressWarnings("unused")
@RestController
public class InfoController {

    @Value("${build.project}")
    private String buildProject;

    @Value("${build.version}")
    private String buildVersion;

    @Value("${build.timestamp}")
    private String buildTimestamp;

    @GetMapping(value = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> info() {
        String body = String.format("{\"name\":\"%s\", \"version\":\"%s\", \"build\":\"%s\", \"healthy\":\"WORKING\"}", buildProject, buildVersion, buildTimestamp);
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        return ResponseEntity.ok(body);
    }
}
