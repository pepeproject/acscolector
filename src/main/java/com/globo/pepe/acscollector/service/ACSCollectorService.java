package com.globo.pepe.acscollector.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimerTask;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.globo.pepe.acscollector.util.ACSCollectorConfiguration;
import com.globo.pepe.acscollector.util.JsonNodeUtil;
import com.globo.pepe.common.services.JsonLoggerService;

@Service
public class ACSCollectorService extends TimerTask {

    private static final Logger logger = LogManager.getLogger(ACSClient.class);


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
                Long timestamp = new Date().getTime();
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

            logger.info("Iniciou da consulta");
            ExecutorService threadPool = Executors.newFixedThreadPool(loadBalances.get("listloadbalancerrulesresponse").get("loadbalancerrule").size());
            ExecutorCompletionService<JsonNode> completionService = new ExecutorCompletionService<>(threadPool);

            List<ExemploCallable> tarefas = new ArrayList<ExemploCallable>();

            for (JsonNode jsonNode : loadBalances.get("listloadbalancerrulesresponse").get("loadbalancerrule")) {
                tarefas.add(new ExemploCallable(30000,acsClient,jsonNode.get("id").asText()));
            }

            try {
                for (ExemploCallable tarefa : tarefas) {
                    completionService.submit(tarefa);
                }
            }catch (Exception e){

            }

            for (int i = 0; i < tarefas.size(); i++) {
                try {
                    JsonNode virtualMachines = completionService.take().get();
                } catch (InterruptedException | ExecutionException ex) {
                    ex.printStackTrace();
                }
            }


       /*     for (JsonNode node : loadBalances.get("listloadbalancerrulesresponse").get("loadbalancerrule")) {

                String id = node.get("id").asText();
           = acsClient.getLoadBalanceInstances(id);
                setInstancesLoadBalance(node, virtualMachines);

                JsonNode autoScaleGroup = acsClient.getAutoScaleByLB(id);
                setAutoScaleGroupLoadBalance(node, autoScaleGroup);
            }*/
            logger.info("Fim da consulta");

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
