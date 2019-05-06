package com.globo.pepe.acscollector.service;

import java.util.concurrent.Callable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ACSCallable implements Callable<JsonNode> {

    private final ACSClient acsClient;
    private final JsonNode loadBalance;

    public ACSCallable(ACSClient acsClient, JsonNode loadBalance) {
        this.acsClient = acsClient;
        this.loadBalance = loadBalance;
    }

    @Override
    public JsonNode call() throws Exception {
        String id = this.loadBalance.get("id").asText();
        
        JsonNode virtualMachines = acsClient.getLoadBalanceInstances(id);
        setInstancesLoadBalance(virtualMachines);

        JsonNode autoScaleGroup = acsClient.getAutoScaleByLB(id);
        setAutoScaleGroupLoadBalance(autoScaleGroup);
        
        return this.loadBalance;
    }

    private void setInstancesLoadBalance(JsonNode virtualMachines){
        if(virtualMachines.get("listloadbalancerruleinstancesresponse") != null && virtualMachines.get("listloadbalancerruleinstancesresponse").get("loadbalancerruleinstance") != null){
            ((ObjectNode) loadBalance).set("virtualMachines",virtualMachines.get("listloadbalancerruleinstancesresponse").get("loadbalancerruleinstance"));
        }
    }
    
    private void setAutoScaleGroupLoadBalance(JsonNode autoScaleGroup){
        if(autoScaleGroup.get("listautoscalevmgroupsresponse") != null && autoScaleGroup.get("listautoscalevmgroupsresponse").get("autoscalevmgroup") != null){
            ((ObjectNode) loadBalance).set("autoScaleGroup",autoScaleGroup.get("listautoscalevmgroupsresponse").get("autoscalevmgroup"));
        }
    }
    
}
