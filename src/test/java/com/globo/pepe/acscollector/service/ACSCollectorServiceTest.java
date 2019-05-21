
package com.globo.pepe.acscollector.service;

import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.globo.pepe.acscollector.ApplicationTests;
import com.globo.pepe.common.services.JsonLoggerService;
import com.globo.pepe.common.services.JsonLoggerService.JsonLogger;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

public class ACSCollectorServiceTest extends ApplicationTests {

    @Autowired
    private ACSCollectorService acsCollectorService;


    private ObjectMapper mapper;

    @Before
    public void setup(){
        this.mapper = new ObjectMapper();

    }


    public ACSCollectorServiceTest() {
        JsonLoggerService jsonLoggerService = Mockito.mock(JsonLoggerService.class);
        JsonLogger jsonLogger = Mockito.mock(JsonLogger.class);
        
        Mockito.when(jsonLogger.put(Mockito.anyString(), Mockito.anyString())).thenReturn(jsonLogger);
        Mockito.when(jsonLogger.sendError()).thenReturn("");
        
        Mockito.when(jsonLoggerService.newLogger(Mockito.any())).thenReturn(jsonLogger);
    }
    
    @Test
    public void getDetailsLoadBalance()throws Exception{
        String loadBalanceString = getDataServiceMock().getResult("listLoadBalancerRules");
        JsonNode loadBalancer = mapper.readTree(loadBalanceString);

        String virtualMachinesString =  getDataServiceMock().getResult("listLoadBalancerRuleInstances");
        JsonNode virtualMachines = mapper.readTree(virtualMachinesString);

        String autoScaleString = getDataServiceMock().getResult("listAutoScaleVmGroups");
        JsonNode autoScale = mapper.readTree(autoScaleString);

        ACSClientService acsClient = Mockito.mock(ACSClientService.class);
        Mockito.when(acsClient.getLoadBalanceInstances(Mockito.anyString())).thenReturn(virtualMachines);
        Mockito.when(acsClient.getAutoScaleByLB(Mockito.anyString())).thenReturn(autoScale);

        acsCollectorService.getDetailsLoadBalance(acsClient,loadBalancer);
        
        for (JsonNode jsonNode : loadBalancer.get("listloadbalancerrulesresponse").get("loadbalancerrule")) {
            assertThat(jsonNode.get("virtualMachines"), Matchers.notNullValue());
            assertThat(jsonNode.get("autoScaleGroup"), Matchers.notNullValue());
        }
    }
}

