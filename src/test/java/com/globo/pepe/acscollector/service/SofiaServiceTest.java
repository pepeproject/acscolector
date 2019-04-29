package com.globo.pepe.acscollector.service;
import static org.junit.Assert.assertThat;
import com.globo.pepe.acscollector.ApplicationTests;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

public class SofiaServiceTest extends ApplicationTests {

    @Autowired
    private SofiaService sofiaService;

    @Test
    public void post(){

    }

    @Test
    public void getRestTemplate(){
      RestTemplate restTemplate =  sofiaService.getRestTemplate();
        assertThat(restTemplate, Matchers.notNullValue());

    }

}
