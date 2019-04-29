package com.globo.pepe.acscollector.service;

import static org.junit.Assert.assertThat;

import br.com.autonomiccs.apacheCloudStack.client.ApacheCloudStackRequest;
import com.globo.pepe.acscollector.ApplicationTests;
import com.globo.pepe.acscollector.util.ACSCollectorConfiguration;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ACSClientTest extends ApplicationTests {

    @Autowired
    ACSCollectorConfiguration configuration;

    @Test
    public void getACSRequestFactory(){
        ACSClient acsClient = new ACSClient(configuration);
        ApacheCloudStackRequest apacheCloudStackRequest = acsClient.getACSRequestFactory("test");
        assertThat(apacheCloudStackRequest, Matchers.notNullValue());

    }

    @Test(expected = RuntimeException.class)
    public void ACSClientException(){
        ACSClient acsClient = new ACSClient(null);
    }


    @Test
    public void ACSClient(){
        ACSClient acsClient = new ACSClient(configuration);
        assertThat(acsClient, Matchers.notNullValue());
    }





}
