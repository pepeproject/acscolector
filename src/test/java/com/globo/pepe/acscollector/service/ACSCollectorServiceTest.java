package com.globo.pepe.acscollector.service;

import static org.junit.Assert.assertThat;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.JsonNode;
import com.globo.pepe.acscollector.ApplicationTests;
import com.globo.pepe.acscollector.util.JsonNodeUtil;
import com.globo.pepe.common.services.JsonLoggerService;
import com.globo.pepe.common.services.JsonLoggerService.JsonLogger;

public class ACSCollectorServiceTest extends ApplicationTests {
    
    private ACSCollectorService acsCollectorService;
    
    public ACSCollectorServiceTest() {
        JsonLoggerService jsonLoggerService = Mockito.mock(JsonLoggerService.class);
        JsonLogger jsonLogger = Mockito.mock(JsonLogger.class);
        
        Mockito.when(jsonLogger.put(Mockito.anyString(), Mockito.anyString())).thenReturn(jsonLogger);
        Mockito.when(jsonLogger.sendError()).thenReturn("");
        
        Mockito.when(jsonLoggerService.newLogger(Mockito.any())).thenReturn(jsonLogger);
        
        this.acsCollectorService = new ACSCollectorService(jsonLoggerService);
    }
    
    @Test
    public void getDetailsLoadBalance()throws Exception{
        String loadBalanceString = getDataServiceMock().getResult("listLoadBalancerRules");
        JsonNode loadBalancer = JsonNodeUtil.desirializerJsonNode(loadBalanceString);

        String virtualMachinesString =  getDataServiceMock().getResult("listLoadBalancerRuleInstances");
        JsonNode virtualMachines = JsonNodeUtil.desirializerJsonNode(virtualMachinesString);

        String autoScaleString = getDataServiceMock().getResult("listAutoScaleVmGroups");
        JsonNode autoScale = JsonNodeUtil.desirializerJsonNode(autoScaleString);

        ACSClient acsClient = Mockito.mock(ACSClient.class);
        Mockito.when(acsClient.getLoadBalanceInstances(Mockito.anyString())).thenReturn(virtualMachines);
        Mockito.when(acsClient.getAutoScaleByLB(Mockito.anyString())).thenReturn(autoScale);

        acsCollectorService.getDetailsLoadBalance(acsClient,loadBalancer);
        
        for (JsonNode jsonNode : loadBalancer.get("listloadbalancerrulesresponse").get("loadbalancerrule")) {
            assertThat(jsonNode.get("virtualMachines"), Matchers.notNullValue());
            assertThat(jsonNode.get("autoScaleGroup"), Matchers.notNullValue());
        }
    }
}
