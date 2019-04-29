
package com.globo.pepe.acscollector.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@SuppressWarnings({"unused", "SameReturnValue"})
@RestController
public class HealthcheckController {

    @GetMapping(value = "/healthcheck.html")
    public ResponseEntity<String> healthcheck() {
        return ResponseEntity.ok("WORKING");
    }
}
