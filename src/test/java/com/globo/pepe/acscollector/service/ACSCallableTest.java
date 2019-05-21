package com.globo.pepe.acscollector.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.globo.pepe.acscollector.ApplicationTests;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ACSCallableTest extends ApplicationTests {

    private ObjectMapper mapper;

    @Before
    public void setup() {
        this.mapper = new ObjectMapper();

    }

    @Test
    public void setInstancesLoadBalance() throws Exception {
        String loadBalanceString = getDataServiceMock().getResult("listLoadBalancerRules");
        JsonNode loadBalancer = mapper.readTree(loadBalanceString);
        
        String virtualMachinesString =  getDataServiceMock().getResult("listLoadBalancerRuleInstances");
        JsonNode virtualMachines = mapper.readTree(virtualMachinesString);

        Assert.assertThat(virtualMachines.get("listloadbalancerruleinstancesresponse"), Matchers.notNullValue());
        Assert.assertThat(virtualMachines.get("listloadbalancerruleinstancesresponse").get("loadbalancerruleinstance"), Matchers.notNullValue());
        
        String autoScaleString = getDataServiceMock().getResult("listAutoScaleVmGroups");
        JsonNode autoScale = mapper.readTree(autoScaleString);

        ACSClientService acsClient = Mockito.mock(ACSClientService.class);
        
        Mockito.when(acsClient.getLoadBalanceInstances(Mockito.anyString())).thenReturn(virtualMachines);
        Mockito.when(acsClient.getAutoScaleByLB(Mockito.anyString())).thenReturn(autoScale);
        
        for (JsonNode jsonNodeVIP : loadBalancer.get("listloadbalancerrulesresponse").get("loadbalancerrule")) {
            ACSCallable acsCallable = new ACSCallable(acsClient, jsonNodeVIP);
            acsCallable.call();

            Assert.assertThat(jsonNodeVIP.get("virtualMachines"), Matchers.notNullValue());
        }
    }


    @Test
    public void setInstancesLoadBalanceMachineNull() throws Exception {
        String virtualMachinesString = getDataServiceMock().getResultMachineNull("listLoadBalancerRuleInstances");
        JsonNode virtualMachines = mapper.readTree(virtualMachinesString);

        String loadBalanceString = getDataServiceMock().getResult("listLoadBalancerRules");
        JsonNode loadBalancer = mapper.readTree(loadBalanceString);

        for (JsonNode jsonNodeVIP : loadBalancer.get("listloadbalancerrulesresponse").get("loadbalancerrule")) {
            ACSCallable acsCallable = new ACSCallable(null, jsonNodeVIP);
            acsCallable.setInstancesLoadBalance(virtualMachines);

            Assert.assertThat(jsonNodeVIP.get("virtualMachines"), Matchers.nullValue());
            Assert.assertThat(virtualMachines.get("listloadbalancerruleinstancesresponse"), Matchers.nullValue());
        }
    }


    @Test
    public void setInstancesLoadBalanceMachineNullLayer() throws Exception {
        String virtualMachinesString = getDataServiceMock().getResultMachineNullTask("listLoadBalancerRuleInstances");
        JsonNode virtualMachines = mapper.readTree(virtualMachinesString);

        Assert.assertThat(virtualMachines.get("listloadbalancerruleinstancesresponse"), Matchers.notNullValue());
        Assert.assertThat(virtualMachines.get("listloadbalancerruleinstancesresponse").get("loadbalancerruleinstance"),
            Matchers.nullValue());

        String loadBalanceString = getDataServiceMock().getResult("listLoadBalancerRules");
        JsonNode loadBalancer = mapper.readTree(loadBalanceString);

        for (JsonNode jsonNodeVIP : loadBalancer.get("listloadbalancerrulesresponse").get("loadbalancerrule")) {
            ACSCallable acsCallable = new ACSCallable(null, jsonNodeVIP);
            acsCallable.setInstancesLoadBalance(virtualMachines);

            Assert.assertThat(jsonNodeVIP.get("virtualMachines"), Matchers.nullValue());
        }
    }

    @Test
    public void setAutoScaleGroupLoadBalance() throws Exception {
        String autoScaleString = getDataServiceMock().getResult("listAutoScaleVmGroups");
        JsonNode autoScale = mapper.readTree(autoScaleString);

        String loadBalanceString = getDataServiceMock().getResult("listLoadBalancerRules");
        JsonNode loadBalancer = mapper.readTree(loadBalanceString);

        for (JsonNode jsonNodeVIP : loadBalancer.get("listloadbalancerrulesresponse").get("loadbalancerrule")) {
            ACSCallable acsCallable = new ACSCallable(null, jsonNodeVIP);
            acsCallable.setAutoScaleGroupLoadBalance(autoScale);

            Assert.assertThat(jsonNodeVIP.get("autoScaleGroup"), Matchers.notNullValue());
        }
    }
}
