package com.globo.pepe.acscollector.runner;

import com.globo.pepe.acscollector.util.ACSCollectorConfiguration;
import com.globo.pepe.acscollector.service.ACSCollectorService;
import java.util.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class ACSRunner {

    @Autowired
    private  ACSCollectorService acsCollectorService;

    @Scheduled(fixedDelayString = "${fixedDelay}")
    public void run(){
        acsCollectorService.run();
    }

}
