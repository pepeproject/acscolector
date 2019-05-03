package com.globo.pepe.acscollector.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
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
        ACSClient acsClient = new ACSClient(configuration, jsonLoggerService);
        JsonNode loadBalances = acsClient.getLoadBalancesByProject(configuration.getProjectId());
        getDetailsLoadBalance(acsClient, loadBalances);
        return loadBalances;
    }

    public void getDetailsLoadBalance(ACSClient acsClient, JsonNode loadBalances) {
        if (loadBalances != null && loadBalances.get("listloadbalancerrulesresponse") != null
                && loadBalances.get("listloadbalancerrulesresponse").get("loadbalancerrule") != null
                && loadBalances.get("listloadbalancerrulesresponse").get("loadbalancerrule").isArray()) {
            
            logger.info("Inicio da consulta");
            
            Instant start = Instant.now();
            
            ExecutorService threadPool = Executors.newFixedThreadPool(
                    loadBalances.get("listloadbalancerrulesresponse").get("loadbalancerrule").size());
            ExecutorCompletionService<JsonNode> completionService = new ExecutorCompletionService<>(threadPool);

            List<ACSCallable> asyncronousTasks = new ArrayList<ACSCallable>();

            for (JsonNode jsonNodeVIP : loadBalances.get("listloadbalancerrulesresponse").get("loadbalancerrule")) {
                asyncronousTasks.add(new ACSCallable(acsClient, jsonNodeVIP));
            }

            for (ACSCallable tarefa : asyncronousTasks) {
                completionService.submit(tarefa);
            }

            for (int i = 0; i < asyncronousTasks.size(); i++) {
                try {
                    completionService.take().get();
                } catch (InterruptedException | ExecutionException e) {
                    jsonLoggerService.newLogger(getClass()).put("short_message", e.getMessage()).sendError();
                }
            }
            
            Instant end = Instant.now();
            Duration timeElapsed = Duration.between(start, end);

            logger.info("Fim da consulta: " + timeElapsed.toMillis());
        }
    }
}
