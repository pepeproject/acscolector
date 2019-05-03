package com.globo.pepe.acscollector.service;

import java.util.Calendar;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimerTask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.globo.pepe.acscollector.util.ACSCollectorConfiguration;
import com.globo.pepe.acscollector.util.JsonNodeUtil;
import com.globo.pepe.common.services.JsonLoggerService;

@Service
public class ACSCollectorService extends TimerTask {

    @Autowired
    private ACSCollectorConfiguration configuration;

    @Autowired
    private TelegrafService telegrafService;

    private final JsonLoggerService jsonLoggerService;

    public ACSCollectorService(JsonLoggerService jsonLoggerService) {
        this.jsonLoggerService = jsonLoggerService;
    }

    @Override
    public void run() {
        JsonNode loadBalances = null;
            try {
                Long timestamp = getTimestampToTelegraf();
                loadBalances = getLoadBalances();
                
                Map<String, Map<String,String>> loadBalancerFormated = JsonNodeUtil.formmaterPostTelegraf(loadBalances);
                
                for (Entry<String, Map<String,String>> vip : loadBalancerFormated.entrySet()) {
                    for (Entry<String, String> vm: vip.getValue().entrySet()) {
                        telegrafService.post(vm.getValue(), timestamp);
                    }
                }
                
            }catch (Exception e){
                jsonLoggerService.newLogger(getClass()).put("short_message", e.getMessage() + ": " + loadBalances).sendError();
            }

    }

    private long getTimestampToTelegraf() {
        return Calendar.getInstance().getTimeInMillis() * 1000L * 1000L;
    }

    private JsonNode getLoadBalances() {
        ACSClient acsClient = new ACSClient(configuration);
        JsonNode loadBalances = acsClient.getLoadBalancesByProject(configuration.getProjectId());
        getDetailsLoadBalance(acsClient, loadBalances);
        return loadBalances;
    }

    public void getDetailsLoadBalance(ACSClient acsClient, JsonNode loadBalances) {
        if(loadBalances != null && loadBalances.get("listloadbalancerrulesresponse") != null
            && loadBalances.get("listloadbalancerrulesresponse").get("loadbalancerrule") != null
            &&loadBalances.get("listloadbalancerrulesresponse").get("loadbalancerrule").isArray()) {
            for (JsonNode node : loadBalances.get("listloadbalancerrulesresponse").get("loadbalancerrule")) {

                String id = node.get("id").asText();
                JsonNode virtualMachines = acsClient.getLoadBalanceInstances(id);
                setInstancesLoadBalance(node, virtualMachines);

                JsonNode autoScaleGroup = acsClient.getAutoScaleByLB(id);
                setAutoScaleGroupLoadBalance(node, autoScaleGroup);
            }
        }
    }

    public void  setInstancesLoadBalance(JsonNode loadBalance,JsonNode virtualMachines){
        if(virtualMachines.get("listloadbalancerruleinstancesresponse") != null && virtualMachines.get("listloadbalancerruleinstancesresponse").get("loadbalancerruleinstance") != null){
            ((ObjectNode) loadBalance).set("virtualMachines",virtualMachines.get("listloadbalancerruleinstancesresponse").get("loadbalancerruleinstance"));
        }
    }

    public void  setAutoScaleGroupLoadBalance(JsonNode loadBalance,JsonNode autoScaleGroup){
        if(autoScaleGroup.get("listautoscalevmgroupsresponse") != null && autoScaleGroup.get("listautoscalevmgroupsresponse").get("autoscalevmgroup") != null){
            ((ObjectNode) loadBalance).set("autoScaleGroup",autoScaleGroup.get("listautoscalevmgroupsresponse").get("autoscalevmgroup"));
        }
    }
}
