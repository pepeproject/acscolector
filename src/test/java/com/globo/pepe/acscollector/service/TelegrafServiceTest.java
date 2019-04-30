package com.globo.pepe.acscollector.service;

import static org.junit.Assert.assertThat;

import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import com.globo.pepe.acscollector.util.ACSCollectorConfiguration;

@RunWith(SpringRunner.class)
@WebMvcTest({TelegrafService.class, ACSCollectorConfiguration.class})
public class TelegrafServiceTest {

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

    @Autowired
    private TelegrafService telegrafService;

    private static ClientAndServer mockServer;

    @BeforeClass
    public static void setupClass() {
        mockServer = ClientAndServer.startClientAndServer(5000);

        mockServer.when(HttpRequest.request().withMethod("POST").withPath("/write").withBody(requestBody("request-ok")))
                .respond(HttpResponse.response().withBody("")
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE).withStatusCode(204));
        
        
    }

    private static String requestBody(String string) {
        return POST_DATA_WITHOUT_TIMESTAMP + " " + "0";
    }
    
    @AfterClass
    public static void cleanup() {
        if (mockServer.isRunning()) {
            mockServer.stop();
        }
    }
    
    @Test
    public void post() {
        telegrafService.post(POST_DATA_WITHOUT_TIMESTAMP, 0L);
    }

    @Test
    public void getRestTemplate() {
        RestTemplate restTemplate = telegrafService.getRestTemplate();
        assertThat(restTemplate, Matchers.notNullValue());
    }

}
