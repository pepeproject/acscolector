package com.globo.pepe.acscollector.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.concurrent.Callable;

public class ACSCallable implements Callable<JsonNode> {

    private final ACSClientService acsClientService;
    private final JsonNode loadBalance;

    public ACSCallable(ACSClientService acsClientService, JsonNode loadBalance) {
        this.acsClientService = acsClientService;
        this.loadBalance = loadBalance;
    }

    @Override
    public JsonNode call() throws Exception {
        String id = this.loadBalance.get("id").asText();

        JsonNode virtualMachines = acsClientService.getLoadBalanceInstances(id);
        setInstancesLoadBalance(virtualMachines);

        JsonNode autoScaleGroup = acsClientService.getAutoScaleByLB(id);
        setAutoScaleGroupLoadBalance(autoScaleGroup);

        return this.loadBalance;
    }

    protected void setInstancesLoadBalance(JsonNode virtualMachines){
        if(virtualMachines.get("listloadbalancerruleinstancesresponse") != null && virtualMachines.get("listloadbalancerruleinstancesresponse").size() > 0 && virtualMachines.get("listloadbalancerruleinstancesresponse").get("loadbalancerruleinstance") != null){
            ((ObjectNode) loadBalance).set("virtualMachines",virtualMachines.get("listloadbalancerruleinstancesresponse").get("loadbalancerruleinstance"));
        }
    }
    
    protected void setAutoScaleGroupLoadBalance(JsonNode autoScaleGroup){
        if(autoScaleGroup.get("listautoscalevmgroupsresponse") != null && autoScaleGroup.get("listautoscalevmgroupsresponse").get("autoscalevmgroup") != null && autoScaleGroup.get("listautoscalevmgroupsresponse").get("autoscalevmgroup").size() > 0){
            ((ObjectNode) loadBalance).set("autoScaleGroup",autoScaleGroup.get("listautoscalevmgroupsresponse").get("autoscalevmgroup"));
        }
    }
}