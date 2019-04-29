package com.globo.pepe.acscollector.util;

import static org.junit.Assert.assertThat;

import com.globo.pepe.acscollector.ApplicationTests;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ACSCollectorConfigurationTest extends ApplicationTests {

    @Autowired
    private ACSCollectorConfiguration acsCollectorConfiguration;

    @Test
    public void getApiKey(){
        String apiKey =  acsCollectorConfiguration.getApiKey();
        assertThat(apiKey, Matchers.instanceOf(String.class));
        assertThat(apiKey, Matchers.notNullValue());
    }

    @Test
    public void getProjectId(){
        String projectId = acsCollectorConfiguration.getProjectId();
        assertThat(projectId, Matchers.instanceOf(String.class));
        assertThat(projectId, Matchers.notNullValue());
    }

    @Test
    public void getUrlACS(){
        String urlACS = acsCollectorConfiguration.getUrlACS();
        assertThat(urlACS, Matchers.instanceOf(String.class));
        assertThat(urlACS, Matchers.notNullValue());
    }

    @Test
    public void getUrlTelegraf(){
        String urlTelegraf = acsCollectorConfiguration.getUrlTelegraf();
        assertThat(urlTelegraf, Matchers.instanceOf(String.class));
        assertThat(urlTelegraf, Matchers.notNullValue());
    }

    @Test
    public void getSecretKey(){
        String secretKey = acsCollectorConfiguration.getSecretKey();
        assertThat(secretKey, Matchers.instanceOf(String.class));
        assertThat(secretKey, Matchers.notNullValue());
    }

}
