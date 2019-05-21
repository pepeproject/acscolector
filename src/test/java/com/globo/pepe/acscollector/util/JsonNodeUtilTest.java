package com.globo.pepe.acscollector.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.globo.pepe.acscollector.ApplicationTests;
import com.globo.pepe.acscollector.service.ACSCallable;
import com.globo.pepe.acscollector.service.ACSClientService;
import java.util.Map;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

public class JsonNodeUtilTest extends ApplicationTests {

    private ObjectMapper mapper;

    @Autowired
    private  JsonNodeUtil jsonNodeUtil;

    @Before
    public void setup(){
        this.mapper = new ObjectMapper();

    }


    @Test
    public void getLoadBalancesByProject() throws Exception {
        String result = getDataServiceMock().getResult("listLoadBalancerRules");
        JsonNode listLoadBalancerRules = mapper.readTree(result);
        assertThat(listLoadBalancerRules, Matchers.notNullValue());
        assertThat(listLoadBalancerRules, Matchers.instanceOf(JsonNode.class));
    }

    @Test
    public void getAutoScaleByLB() throws Exception {
        String result = getDataServiceMock().getResult("listAutoScaleVmGroups");
        JsonNode listAutoScaleVmGroups =  mapper.readTree(result);
        assertThat(listAutoScaleVmGroups, Matchers.notNullValue());
        assertThat(listAutoScaleVmGroups, Matchers.instanceOf(JsonNode.class));
    }

    @Test
    public void getLoadBalanceInstances() throws Exception {
        String result =  getDataServiceMock().getResult("listLoadBalancerRuleInstances");
        JsonNode listLoadBalancerRuleInstances = mapper.readTree(result);
        assertThat(listLoadBalancerRuleInstances, Matchers.notNullValue());
        assertThat(listLoadBalancerRuleInstances, Matchers.instanceOf(JsonNode.class));
    }

    @Test
    public void formmaterPostTelegraf() throws Exception{
        String loadBalanceString = getDataServiceMock().getResult("listLoadBalancerRules");
        JsonNode loadBalancer = mapper.readTree(loadBalanceString);
        
        String virtualMachinesString =  getDataServiceMock().getResult("listLoadBalancerRuleInstances");
        JsonNode virtualMachines = mapper.readTree(virtualMachinesString);
        
        String autoScaleString = getDataServiceMock().getResult("listAutoScaleVmGroups");
        JsonNode autoScale = mapper.readTree(autoScaleString);

        ACSClientService acsClient = Mockito.mock(ACSClientService.class);
        
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
        
        Map<String, Map<String,String>> mapVIPPost = jsonNodeUtil.formmaterPostTelegraf(loadBalancer);

        assertThat(mapVIPPost.get("domain.com"), Matchers.notNullValue());
        assertThat(mapVIPPost.get("domain.com").get("00000000-0000-0000-0000-00000000000"), Matchers.notNullValue());
        
        String postVIPDomainDotComToTelegraf = postToTelegrafVIPDomainDotCom();
        
        assertEquals(mapVIPPost.get("domain.com").get("00000000-0000-0000-0000-00000000000"),
                postVIPDomainDotComToTelegraf);
    }

    private String postToTelegrafVIPDomainDotCom() {
        String postVIPDomainDotComToTelegraf = "pepe_acs_metrics," + "vip_id=00000000-0000-0000-0000-00000000000,"
                + "vip_name=domain.com," + "vm_name=bus-maq-9115-20190416185504,"
                + "vm_id=00000000-0000-0000-0000-00000000000," + "vm_project=Project,"
                + "project_id=00000000-0000-0000-0000-00000000000," + "vm_state=Running,"
                + "vm_created=2019-04-16T18:55:05-0300," + "vm_ip_address=127.0.0.1" + " " + "autoscale_minmembers=2,"
                + "autoscale_maxmembers=2," + "autoscale_count=2";
        return postVIPDomainDotComToTelegraf;
    }

}

