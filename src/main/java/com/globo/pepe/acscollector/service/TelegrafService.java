package com.globo.pepe.acscollector.service;

import com.globo.pepe.common.services.JsonLoggerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TelegrafService {

    private @Value("${telegraf.url}")
    String urlTelegraf;

    private final RestTemplate restTemplate;
    
    private final JsonLoggerService jsonLoggerService;

    public TelegrafService(JsonLoggerService jsonLoggerService) {
        this.restTemplate = new RestTemplate();
        this.jsonLoggerService = jsonLoggerService;
    }

    public void post(String metric, Long timestamp) {
        StringBuffer stringBuffer = new StringBuffer(metric);
        stringBuffer.append(" ").append(timestamp);
        String metricWithTimestamp = stringBuffer.toString();

        HttpEntity<String> entity = new HttpEntity<>(metricWithTimestamp);
        
        try {
            restTemplate.exchange(urlTelegraf, HttpMethod.POST, entity, String.class);
        } catch (Exception e) {
            jsonLoggerService.newLogger(getClass()).put("short_message", e.getMessage() + ": " + "ao enviar métricas: (" + metricWithTimestamp+ ")").sendError();
        }
    }

}