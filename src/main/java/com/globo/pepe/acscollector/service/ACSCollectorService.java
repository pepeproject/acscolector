package com.globo.pepe.acscollector.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.globo.pepe.acscollector.util.JsonNodeUtil;
import com.globo.pepe.common.services.JsonLoggerService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ACSCollectorService extends TimerTask {

    private static final Logger logger = LogManager.getLogger(ACSCollectorService.class);

    private final TelegrafService telegrafService;
    private final ACSClient acsClient;
    private final JsonLoggerService jsonLoggerService;

    public ACSCollectorService(JsonLoggerService jsonLoggerService, ACSClient acsClient, TelegrafService telegrafService) {
        this.jsonLoggerService = jsonLoggerService;
        this.acsClient = acsClient;
        this.telegrafService = telegrafService;
    }


    @Override
    @Scheduled(fixedDelayString = "${acs_collector.fixedDelay}")
    public void run() {
        try {
            Instant start = Instant.now();

            Long timestamp = getTimestampToTelegraf();

            JsonNode loadBalances = getLoadBalances();

            Map<String, Map<String, String>> loadBalancerFormated = JsonNodeUtil.formmaterPostTelegraf(loadBalances);

            for (Entry<String, Map<String, String>> vip : loadBalancerFormated.entrySet()) {
                for (Entry<String, String> vm : vip.getValue().entrySet()) {
                        telegrafService.post(vm.getValue(), timestamp);
                }
            }

            Instant end = Instant.now();

            logger.info("Métricas enviadas em: " + (end.toEpochMilli() - start.toEpochMilli()) + "ms");
        } catch (Exception e) {
             jsonLoggerService.newLogger(getClass()).put("short_message", e.getMessage()).sendError();
        }
    }

    private long getTimestampToTelegraf() {
        return Calendar.getInstance().getTimeInMillis() * 1000L * 1000L;
    }


    private JsonNode getLoadBalances() throws Exception {
        JsonNode loadBalances = acsClient.getLoadBalancesByProject();
        getDetailsLoadBalance(acsClient, loadBalances);
        return loadBalances;
    }

    public void getDetailsLoadBalance(ACSClient acsClient, JsonNode loadBalances) {
        if (loadBalances != null && loadBalances.get("listloadbalancerrulesresponse") != null
            && loadBalances.get("listloadbalancerrulesresponse").get("loadbalancerrule") != null
            && loadBalances.get("listloadbalancerrulesresponse").get("loadbalancerrule").isArray()) {

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
            threadPool.shutdown();
        }
    }
}
