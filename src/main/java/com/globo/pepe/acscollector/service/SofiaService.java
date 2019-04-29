package com.globo.pepe.acscollector.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.globo.pepe.acscollector.util.ACSCollectorConfiguration;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SofiaService {

    @Autowired
    private ACSCollectorConfiguration configuration;

    RestTemplate restTemplate;

    public SofiaService(){
        this.restTemplate  = getRestTemplate();
    }

    public void post(JsonNode metrics){
        HttpEntity<JsonNode> entity = getJsonNodeHttpEntity();
        restTemplate.exchange(configuration.getUrlSofia(), HttpMethod.POST, entity, JsonNode.class).getBody();
    }

    private HttpEntity<JsonNode> getJsonNodeHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        return new HttpEntity<JsonNode>(headers);
    }

    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

}
