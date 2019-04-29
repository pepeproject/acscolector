package com.globo.pepe.acscollector.service;

import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.globo.pepe.acscollector.ApplicationTests;
import com.globo.pepe.acscollector.util.ACSCollectorConfiguration;
import com.globo.pepe.acscollector.util.JsonNodeUtil;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

public class ACSCollectorServiceTest extends ApplicationTests {

    @Autowired
    private ACSCollectorConfiguration configuration;

    private ACSCollectorService acsCollectorService = new ACSCollectorService();

    @Test
    public void setInstancesLoadBalance() throws Exception{
        String virtualMachinesString =  getDataServiceMock().getResult("listLoadBalancerRuleInstances");
        JsonNode virtualMachines = JsonNodeUtil.desirializerJsonNode(virtualMachinesString);

        String loadBalanceString = getDataServiceMock().getResult("listLoadBalancerRules");
        JsonNode loadBalancer = JsonNodeUtil.desirializerJsonNode(loadBalanceString);

        acsCollectorService.setInstancesLoadBalance(loadBalancer,virtualMachines);
        assertThat(loadBalancer.get("virtualMachines"), Matchers.notNullValue());
        assertThat(virtualMachines.get("listloadbalancerruleinstancesresponse"), Matchers.notNullValue());
        assertThat(virtualMachines.get("listloadbalancerruleinstancesresponse").get("loadbalancerruleinstance"), Matchers.notNullValue());
    }


    @Test
    public void setInstancesLoadBalanceMachineNull() throws Exception{
        String virtualMachinesString =  getDataServiceMock().getResultMachineNull("listLoadBalancerRuleInstances");
        JsonNode virtualMachines = JsonNodeUtil.desirializerJsonNode(virtualMachinesString);

        String loadBalanceString = getDataServiceMock().getResult("listLoadBalancerRules");
        JsonNode loadBalancer = JsonNodeUtil.desirializerJsonNode(loadBalanceString);

        acsCollectorService.setInstancesLoadBalance(loadBalancer,virtualMachines);
        assertThat(loadBalancer.get("virtualMachines"), Matchers.nullValue());
        assertThat(virtualMachines.get("listloadbalancerruleinstancesresponse"), Matchers.nullValue());
    }


    @Test
    public void setInstancesLoadBalanceMachineNullLayer() throws Exception{
        String virtualMachinesString =  getDataServiceMock().getResultMachineNullTask("listLoadBalancerRuleInstances");
        JsonNode virtualMachines = JsonNodeUtil.desirializerJsonNode(virtualMachinesString);

        String loadBalanceString = getDataServiceMock().getResult("listLoadBalancerRules");
        JsonNode loadBalancer = JsonNodeUtil.desirializerJsonNode(loadBalanceString);

        acsCollectorService.setInstancesLoadBalance(loadBalancer,virtualMachines);
        assertThat(loadBalancer.get("virtualMachines"), Matchers.nullValue());
        assertThat(virtualMachines.get("listloadbalancerruleinstancesresponse"), Matchers.notNullValue());
        assertThat(virtualMachines.get("listloadbalancerruleinstancesresponse").get("loadbalancerruleinstance"), Matchers.nullValue());
    }

    @Test
    public void  setAutoScaleGroupLoadBalance()throws Exception{

        String autoScaleString = getDataServiceMock().getResult("listAutoScaleVmGroups");
        JsonNode autoScale = JsonNodeUtil.desirializerJsonNode(autoScaleString);

        String loadBalanceString = getDataServiceMock().getResult("listLoadBalancerRules");
        JsonNode loadBalancer = JsonNodeUtil.desirializerJsonNode(loadBalanceString);

        acsCollectorService.setAutoScaleGroupLoadBalance(loadBalancer,autoScale);
        assertThat(loadBalancer, Matchers.notNullValue());

    }

    @Test
    public void getDetailsLoadBalance()throws Exception{
        String loadBalanceString = getDataServiceMock().getResult("listLoadBalancerRules");
        JsonNode loadBalancer = JsonNodeUtil.desirializerJsonNode(loadBalanceString);

        String virtualMachinesString =  getDataServiceMock().getResult("listLoadBalancerRuleInstances");
        JsonNode virtualMachines = JsonNodeUtil.desirializerJsonNode(virtualMachinesString);
        acsCollectorService.setInstancesLoadBalance(loadBalancer,virtualMachines);


        String autoScaleString = getDataServiceMock().getResult("listAutoScaleVmGroups");
        JsonNode autoScale = JsonNodeUtil.desirializerJsonNode(autoScaleString);
        acsCollectorService.setAutoScaleGroupLoadBalance(loadBalancer,autoScale);

        ACSClient acsClient = Mockito.mock(ACSClient.class);
        Mockito.when(acsClient.getLoadBalanceInstances(Mockito.anyString())).thenReturn(virtualMachines);
        Mockito.when(acsClient.getAutoScaleByLB(Mockito.anyString())).thenReturn(autoScale);

        acsCollectorService.getDetailsLoadBalance(acsClient,loadBalancer);
        assertThat(loadBalancer.get("virtualMachines"), Matchers.notNullValue());
        assertThat(loadBalancer.get("autoScaleGroup"), Matchers.notNullValue());
    }


}
