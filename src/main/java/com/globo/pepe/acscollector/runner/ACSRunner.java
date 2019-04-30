package com.globo.pepe.acscollector.runner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.globo.pepe.acscollector.service.ACSCollectorService;

@Configuration
@EnableScheduling
public class ACSRunner {

    @Autowired
    private ACSCollectorService acsCollectorService;

    @Scheduled(fixedDelayString = "${acs_collector.fixedDelay}")
    public void run() {
        acsCollectorService.run();
    }

}
