package com.globo.pepe.acscollector.service;
import static org.junit.Assert.assertThat;
import com.globo.pepe.acscollector.ApplicationTests;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

public class TelegrafServiceTest extends ApplicationTests {

    @Autowired
    private TelegrafService telegrafService;

    @Test
    public void post(){

    }

    @Test
    public void getRestTemplate(){
      RestTemplate restTemplate =  telegrafService.getRestTemplate();
      assertThat(restTemplate, Matchers.notNullValue());
    }

}
