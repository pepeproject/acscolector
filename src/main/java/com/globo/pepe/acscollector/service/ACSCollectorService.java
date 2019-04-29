package com.globo.pepe.acscollector.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.globo.pepe.acscollector.util.ACSCollectorConfiguration;
import java.util.TimerTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ACSCollectorService extends TimerTask {

    private static final Logger logger = LogManager.getLogger(ACSCollectorService.class);

    @Autowired
    private ACSCollectorConfiguration configuration;

    @Autowired
    private TelegrafService telegrafService;

    @Override
    public void run() {
            try {
                logger.info("comecou a enviar");
                JsonNode loadBalances = getLoadBalances();
                telegrafService.post(loadBalances);
                logger.info("terminou de enviar");
            }catch (Exception e){
                logger.error(e.getMessage(),e);
            }

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
