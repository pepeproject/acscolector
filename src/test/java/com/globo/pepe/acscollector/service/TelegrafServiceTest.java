package com.globo.pepe.acscollector.service;

import static org.junit.Assert.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import com.globo.pepe.acscollector.ApplicationTests;

public class TelegrafServiceTest extends ApplicationTests {

    private static final String POST_DATA_WITHOUT_TIMESTAMP = "unit_test_metrics,"
            + "vip_id=00000000-0000-0000-0000-00000000000,"
            + "vip_name=vip.name.test.domain.com,"
            + "vm_name=vip.name.test,"
            + "vm_id=00000000-0000-0000-0000-00000000000,"
            + "vm_project=Test Test,"
            + "project_id=00000000-0000-0000-0000-00000000000,"
            + "vm_state=state_test,"
            + "vm_created=0000-00-00T00:00:00-0300,"
            + "vm_ip_address=00.000.000.00"
            + " "
            + "autoscale_minmembers=1,"
            + "autoscale_maxmembers=1,"
            + "autoscale_count=1";
    
    private static final String POST_DATA_INVALID_WITHOUT_TIMESTAMP = "invalid_metric";

    @Autowired
    private TelegrafService telegrafService;

    private static ClientAndServer mockServer;

    @BeforeClass
    public static void setupClass() {
        mockServer = ClientAndServer.startClientAndServer(5000);

        mockServer
                .when(HttpRequest.request().withMethod("POST").withPath("/write")
                        .withBody(addTimeStamp(POST_DATA_WITHOUT_TIMESTAMP)))
                .respond(HttpResponse.response().withBody("")
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE).withStatusCode(NO_CONTENT.value()));

        mockServer
                .when(HttpRequest.request().withMethod("POST").withPath("/write")
                        .withBody(addTimeStamp(POST_DATA_INVALID_WITHOUT_TIMESTAMP)))
                .respond(HttpResponse.response().withBody("{\"error\":\"http: bad request\"}")
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE).withStatusCode(BAD_REQUEST.value()));
    }

    private static String addTimeStamp(String postWithoutTimeStamp) {
        return postWithoutTimeStamp + " " + "0";
    }
    
    @AfterClass
    public static void cleanup() {
        if (mockServer.isRunning()) {
            mockServer.stop();
        }
    }
    
    @Test
    public void postValidData() {
        telegrafService.post(POST_DATA_WITHOUT_TIMESTAMP, 0L);
        
        mockServer.verify(HttpRequest.request().withPath("/write").withBody(addTimeStamp(POST_DATA_WITHOUT_TIMESTAMP)));
    }

    @Test
    public void postInvalidData() throws Exception {
        telegrafService.post(POST_DATA_INVALID_WITHOUT_TIMESTAMP, 0L);
        
        mockServer.verify(HttpRequest.request().withPath("/write").withBody(addTimeStamp(POST_DATA_INVALID_WITHOUT_TIMESTAMP)));
    }
    
    @Test
    public void getRestTemplate() {
        RestTemplate restTemplate = telegrafService.getRestTemplate();
        assertThat(restTemplate, Matchers.notNullValue());
    }

}
