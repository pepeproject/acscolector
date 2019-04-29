package com.globo.pepe.acscollector.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.globo.pepe.acscollector.util.ACSCollectorConfiguration;

@Service
public class TelegrafService {

    @Autowired
    private ACSCollectorConfiguration configuration;

    RestTemplate restTemplate;

    public TelegrafService(){
        this.restTemplate  = getRestTemplate();
    }

    public void post(String metric, Long timestamp){
        StringBuffer stringBuffer = new StringBuffer(metric);
        stringBuffer.append(" ").append(timestamp);
        String metricWithTimestamp = stringBuffer.toString();
        
        HttpEntity<String> entity = new HttpEntity<>(metricWithTimestamp);
        ResponseEntity<String> response = restTemplate.exchange(configuration.getUrlTelegraf(), HttpMethod.POST, entity, String.class);
    }

    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

}
