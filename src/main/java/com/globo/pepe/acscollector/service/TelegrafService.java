package com.globo.pepe.acscollector.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.globo.pepe.acscollector.util.ACSCollectorConfiguration;
import com.globo.pepe.common.services.JsonLoggerService;

@Service
public class TelegrafService {

    private static final Logger logger = LogManager.getLogger(TelegrafService.class);
    
    @Autowired
    private ACSCollectorConfiguration configuration;

    RestTemplate restTemplate;
    
    private final JsonLoggerService jsonLoggerService;

    public TelegrafService(JsonLoggerService jsonLoggerService) {
        this.restTemplate = getRestTemplate();
        this.jsonLoggerService = jsonLoggerService;
    }

    public void post(String metric, Long timestamp) {
        StringBuffer stringBuffer = new StringBuffer(metric);
        stringBuffer.append(" ").append(timestamp);
        String metricWithTimestamp = stringBuffer.toString();

        HttpEntity<String> entity = new HttpEntity<>(metricWithTimestamp);
        
        try {
            restTemplate.exchange(configuration.getUrlTelegraf(), HttpMethod.POST, entity, String.class);
        }
        catch (Exception e) {
            jsonLoggerService.newLogger(getClass()).put("short_message", e.getMessage() + ": " + "ao enviar métricas: (" + metricWithTimestamp+ ")").sendError();
        }
    }

    @Bean
    public RestTemplate getRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new RestTemplateResponseErrorHandler());
        return restTemplate;
    }

}
