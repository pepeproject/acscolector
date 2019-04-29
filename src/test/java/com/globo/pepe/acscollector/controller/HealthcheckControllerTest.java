package com.globo.pepe.acscollector.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import com.globo.pepe.acscollector.ApplicationTests;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc(secure = false)
public class HealthcheckControllerTest extends ApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void healthCheckTest() throws Exception {
        this.mockMvc.perform(get("/healthcheck.html"))
            .andExpect(content().string(Matchers.containsString("WORKING")));

    }

}
