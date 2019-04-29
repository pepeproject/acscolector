package com.globo.pepe.acscollector.service;


import br.com.autonomiccs.apacheCloudStack.client.ApacheCloudStackClient;
import br.com.autonomiccs.apacheCloudStack.client.ApacheCloudStackRequest;
import br.com.autonomiccs.apacheCloudStack.client.beans.ApacheCloudStackUser;
import com.fasterxml.jackson.databind.JsonNode;
import com.globo.pepe.acscollector.util.ACSCollectorConfiguration;
import com.globo.pepe.acscollector.util.JsonNodeUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;


public class ACSClient {

    private static final Logger logger = LogManager.getLogger(ACSClient.class);

    private ApacheCloudStackUser apacheCloudStackUser;
    private ApacheCloudStackClient apacheCloudStackClient;

    @Autowired
    private ACSCollectorConfiguration configuration;

    public ACSClient(ACSCollectorConfiguration configuration) {
        apacheCloudStackUser = new ApacheCloudStackUser(configuration.getSecretKey(), configuration.getApiKey());
        apacheCloudStackClient = new ApacheCloudStackClient(configuration.getUrlACS(), apacheCloudStackUser);
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
            logger.error(e.getMessage(),e);
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
            logger.error(e.getMessage(),e);
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
            logger.error(e.getMessage(),e);
        }finally {
            return result;
        }
    }

    protected JsonNode executeACScommand(ApacheCloudStackRequest apacheCloudStackRequest) throws Exception{
        String response = apacheCloudStackClient.executeRequest(apacheCloudStackRequest);
        JsonNode jsonNode = JsonNodeUtil.desirializerJsonNode(response);
        return jsonNode;
    }

}
