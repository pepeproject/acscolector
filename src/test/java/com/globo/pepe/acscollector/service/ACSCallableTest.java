package com.globo.pepe.acscollector.service;

import static org.junit.Assert.assertThat;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.JsonNode;
import com.globo.pepe.acscollector.ApplicationTests;
import com.globo.pepe.acscollector.util.JsonNodeUtil;

public class ACSCallableTest extends ApplicationTests {
    
    @Test
    public void setInstancesLoadBalance() throws Exception{
        String loadBalanceString = getDataServiceMock().getResult("listLoadBalancerRules");
        JsonNode loadBalancer = JsonNodeUtil.deserializerJsonNode(loadBalanceString);
        
        String virtualMachinesString =  getDataServiceMock().getResult("listLoadBalancerRuleInstances");
        JsonNode virtualMachines = JsonNodeUtil.deserializerJsonNode(virtualMachinesString);
        
        String autoScaleString = getDataServiceMock().getResult("listAutoScaleVmGroups");
        JsonNode autoScale = JsonNodeUtil.deserializerJsonNode(autoScaleString);

        ACSClient acsClient = Mockito.mock(ACSClient.class);
        
        Mockito.when(acsClient.getLoadBalanceInstances(Mockito.anyString())).thenReturn(virtualMachines);
        Mockito.when(acsClient.getAutoScaleByLB(Mockito.anyString())).thenReturn(autoScale);
        
        for (JsonNode jsonNodeVIP : loadBalancer.get("listloadbalancerrulesresponse").get("loadbalancerrule")) {
            ACSCallable acsCallable = new ACSCallable(acsClient, jsonNodeVIP);
            acsCallable.call();
        }
        
        for (JsonNode jsonNodeVIP : loadBalancer.get("listloadbalancerrulesresponse").get("loadbalancerrule")) {
            assertThat(jsonNodeVIP.get("virtualMachines"), Matchers.notNullValue());
        }
        
        assertThat(virtualMachines.get("listloadbalancerruleinstancesresponse"), Matchers.notNullValue());
        assertThat(virtualMachines.get("listloadbalancerruleinstancesresponse").get("loadbalancerruleinstance"), Matchers.notNullValue());
    }

}
