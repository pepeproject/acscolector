package com.globo.pepe.acscollector.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.globo.pepe.acscollector.util.ACSCollectorConfiguration;
import com.globo.pepe.acscollector.util.JsonNodeUtil;
import com.globo.pepe.common.services.JsonLoggerService;

import br.com.autonomiccs.apacheCloudStack.client.ApacheCloudStackClient;
import br.com.autonomiccs.apacheCloudStack.client.ApacheCloudStackRequest;
import br.com.autonomiccs.apacheCloudStack.client.beans.ApacheCloudStackUser;

public class ACSClient {

    private JsonLoggerService jsonLoggerService;

    private ApacheCloudStackUser apacheCloudStackUser;
    private ApacheCloudStackClient apacheCloudStackClient;

    public ACSClient(ACSCollectorConfiguration configuration, JsonLoggerService jsonLoggerService) {
        this.jsonLoggerService = jsonLoggerService;
        this.apacheCloudStackUser = new ApacheCloudStackUser(configuration.getSecretKey(), configuration.getApiKey());
        this.apacheCloudStackClient = new ApacheCloudStackClient(configuration.getUrlACS(), apacheCloudStackUser);
    }

    public ApacheCloudStackRequest getACSRequestFactory(String command) {
        ApacheCloudStackRequest apacheCloudStackRequest = new ApacheCloudStackRequest(command);
        apacheCloudStackRequest.addParameter("response", "json");
        apacheCloudStackRequest.addParameter("listAll", "true");
        return apacheCloudStackRequest;
    }

    public JsonNode getLoadBalanceInstances(String loadBalancerId){
        JsonNode result = null;
        try {
            ApacheCloudStackRequest apacheCloudStackRequest = getACSRequestFactory("listLoadBalancerRuleInstances");
            apacheCloudStackRequest.addParameter("id", loadBalancerId);
            result = executeACScommand(apacheCloudStackRequest);
        }catch (Exception e){
            jsonLoggerService.newLogger(getClass()).put("short_message", e.getMessage()).sendError();
        }finally {
            return result;
        }
    }

    public JsonNode getLoadBalancesByProject(String  projectId){
        JsonNode result = null;
        try {
            ApacheCloudStackRequest apacheCloudStackRequest = getACSRequestFactory("listLoadBalancerRules");
            apacheCloudStackRequest.addParameter("projectid", projectId);
            result = executeACScommand(apacheCloudStackRequest);
        }catch (Exception e){
            jsonLoggerService.newLogger(getClass()).put("short_message", e.getMessage()).sendError();
        }finally {
            return result;
        }
    }

    public JsonNode getAutoScaleByLB(String loadBalanceId){
        JsonNode result = null;
        try {
            ApacheCloudStackRequest apacheCloudStackRequest = getACSRequestFactory("listAutoScaleVmGroups");
            apacheCloudStackRequest.addParameter("lbruleid", loadBalanceId);
            result = executeACScommand(apacheCloudStackRequest);
        }catch (Exception e){
            jsonLoggerService.newLogger(getClass()).put("short_message", e.getMessage()).sendError();
        }finally {
            return result;
        }
    }

    protected JsonNode executeACScommand(ApacheCloudStackRequest apacheCloudStackRequest) throws Exception{
        String response = apacheCloudStackClient.executeRequest(apacheCloudStackRequest);
        JsonNode jsonNode = JsonNodeUtil.deserializerJsonNode(response);
        return jsonNode;
    }

}
