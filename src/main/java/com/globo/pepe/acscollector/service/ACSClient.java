package com.globo.pepe.acscollector.service;

import br.com.autonomiccs.apacheCloudStack.client.ApacheCloudStackClient;
import br.com.autonomiccs.apacheCloudStack.client.ApacheCloudStackRequest;
import br.com.autonomiccs.apacheCloudStack.client.beans.ApacheCloudStackUser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ACSClient {


    private @Value("${acs.project_id}") String projectId;

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    private   RestTemplate restTemplate;
    private ObjectMapper mapper;
    private final  String urlACS;
    private ApacheCloudStackUser apacheCloudStackUser;
    private ApacheCloudStackClient apacheCloudStackClient;
    private ApacheCloudStackRequest apacheCloudStackRequestListLoadBalancerRuleInstances;
    private ApacheCloudStackRequest apacheCloudStackRequestListLoadBalancerRules;
    private ApacheCloudStackRequest apacheCloudStackRequestListAutoScaleVmGroups;

    public  ACSClient(@Value("${acs.url}") String urlACS,  @Value("${acs.api_key}")
        String apiKey, @Value("${acs.secret_key}") String secretKey) {
        this.apacheCloudStackUser = new ApacheCloudStackUser(secretKey, apiKey);
        this.apacheCloudStackClient = new ApacheCloudStackClient(urlACS, apacheCloudStackUser);
        this.mapper = new ObjectMapper();
        this.urlACS = urlACS;
    }

    private ApacheCloudStackRequest getACSRequestFactory(String command) {
        ApacheCloudStackRequest apacheCloudStackRequest = new ApacheCloudStackRequest(command);
        apacheCloudStackRequest.addParameter("response", "json");
        apacheCloudStackRequest.addParameter("listAll", "true");
        return apacheCloudStackRequest;
    }

    public JsonNode getLoadBalanceInstances(String loadBalancerId) throws Exception {
        this.apacheCloudStackRequestListLoadBalancerRuleInstances  = getACSRequestFactory("listLoadBalancerRuleInstances");
        apacheCloudStackRequestListLoadBalancerRuleInstances.addParameter("id", loadBalancerId);
        return executeACScommand(apacheCloudStackRequestListLoadBalancerRuleInstances);
    }

    public JsonNode getLoadBalancesByProject() throws Exception {
        this.apacheCloudStackRequestListLoadBalancerRules =  getACSRequestFactory("listLoadBalancerRules");
        apacheCloudStackRequestListLoadBalancerRules.addParameter("projectid", projectId);
        return executeACScommand(apacheCloudStackRequestListLoadBalancerRules);
    }

    public JsonNode getAutoScaleByLB(String loadBalanceId) throws Exception {
        this.apacheCloudStackRequestListAutoScaleVmGroups =  getACSRequestFactory("listAutoScaleVmGroups");
        apacheCloudStackRequestListAutoScaleVmGroups.addParameter("lbruleid", loadBalanceId);
        return executeACScommand(apacheCloudStackRequestListAutoScaleVmGroups);
    }

    protected JsonNode executeACScommand(ApacheCloudStackRequest apacheCloudStackRequest) throws Exception {
        String response = apacheCloudStackClient.executeRequest(apacheCloudStackRequest);
        return  mapper.readTree(response);
    }

}
