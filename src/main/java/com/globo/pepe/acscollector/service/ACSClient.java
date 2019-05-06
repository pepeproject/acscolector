package com.globo.pepe.acscollector.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.globo.pepe.acscollector.util.ACSCollectorConfiguration;
import com.globo.pepe.acscollector.util.JsonNodeUtil;

import br.com.autonomiccs.apacheCloudStack.client.ApacheCloudStackClient;
import br.com.autonomiccs.apacheCloudStack.client.ApacheCloudStackRequest;
import br.com.autonomiccs.apacheCloudStack.client.beans.ApacheCloudStackUser;

public class ACSClient {

    private ApacheCloudStackUser apacheCloudStackUser;
    private ApacheCloudStackClient apacheCloudStackClient;

    public ACSClient(ACSCollectorConfiguration configuration) {
        this.apacheCloudStackUser = new ApacheCloudStackUser(configuration.getSecretKey(), configuration.getApiKey());
        this.apacheCloudStackClient = new ApacheCloudStackClient(configuration.getUrlACS(), apacheCloudStackUser);
    }

    public ApacheCloudStackRequest getACSRequestFactory(String command) {
        ApacheCloudStackRequest apacheCloudStackRequest = new ApacheCloudStackRequest(command);
        apacheCloudStackRequest.addParameter("response", "json");
        apacheCloudStackRequest.addParameter("listAll", "true");
        return apacheCloudStackRequest;
    }

    public JsonNode getLoadBalanceInstances(String loadBalancerId) throws Exception{
        ApacheCloudStackRequest apacheCloudStackRequest = getACSRequestFactory("listLoadBalancerRuleInstances");
        apacheCloudStackRequest.addParameter("id", loadBalancerId);
        return executeACScommand(apacheCloudStackRequest);
    }

    public JsonNode getLoadBalancesByProject(String  projectId) throws Exception{
        ApacheCloudStackRequest apacheCloudStackRequest = getACSRequestFactory("listLoadBalancerRules");
        apacheCloudStackRequest.addParameter("projectid", projectId);
        return executeACScommand(apacheCloudStackRequest);
    }

    public JsonNode getAutoScaleByLB(String loadBalanceId) throws Exception{
        ApacheCloudStackRequest apacheCloudStackRequest = getACSRequestFactory("listAutoScaleVmGroups");
        apacheCloudStackRequest.addParameter("lbruleid", loadBalanceId);
        return executeACScommand(apacheCloudStackRequest);
    }

    protected JsonNode executeACScommand(ApacheCloudStackRequest apacheCloudStackRequest) throws Exception{
        String response = apacheCloudStackClient.executeRequest(apacheCloudStackRequest);
        JsonNode jsonNode = JsonNodeUtil.deserializerJsonNode(response);
        return jsonNode;
    }

}
