package com.globo.pepe.acscollector.util;

import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.globo.pepe.acscollector.ApplicationTests;
import org.hamcrest.Matchers;
import org.junit.Test;
public class JsonNodeUtilTest extends ApplicationTests {


    @Test
    public void getLoadBalancesByProject() throws Exception {
        String result = getDataServiceMock().getResult("listLoadBalancerRules");
        JsonNode listLoadBalancerRules = JsonNodeUtil.desirializerJsonNode(result);
        assertThat(listLoadBalancerRules, Matchers.notNullValue());
        assertThat(listLoadBalancerRules, Matchers.instanceOf(JsonNode.class));
    }

    @Test
    public void getAutoScaleByLB() throws Exception {
        String result = getDataServiceMock().getResult("listAutoScaleVmGroups");
        JsonNode listAutoScaleVmGroups = JsonNodeUtil.desirializerJsonNode(result);
        assertThat(listAutoScaleVmGroups, Matchers.notNullValue());
        assertThat(listAutoScaleVmGroups, Matchers.instanceOf(JsonNode.class));
    }

    @Test
    public void getLoadBalanceInstances() throws Exception {
        String result =  getDataServiceMock().getResult("listLoadBalancerRuleInstances");
        JsonNode listLoadBalancerRuleInstances = JsonNodeUtil.desirializerJsonNode(result);
        assertThat(listLoadBalancerRuleInstances, Matchers.notNullValue());
        assertThat(listLoadBalancerRuleInstances, Matchers.instanceOf(JsonNode.class));
    }

}
