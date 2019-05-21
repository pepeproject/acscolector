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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ACSCollectorService extends TimerTask {

    private static final Logger logger = LogManager.getLogger(ACSCollectorService.class);

    private final TelegrafService telegrafService;
    private final ACSClientService acsClientService;
    private final JsonLoggerService jsonLoggerService;
    private final JsonNodeUtil jsonNodeUtil;
    private final String projectId;


    public ACSCollectorService(JsonLoggerService jsonLoggerService, ACSClientService acsClientService,
        TelegrafService telegrafService, JsonNodeUtil jsonNodeUtil, @Value("${acs.project_id}") String projectId) {
        this.jsonLoggerService = jsonLoggerService;
        this.acsClientService = acsClientService;
        this.telegrafService = telegrafService;
        this.jsonNodeUtil = jsonNodeUtil;
        this.projectId = projectId;
    }

    @Override
    @Scheduled(fixedDelayString = "${acs_collector.fixedDelay}")
    public void run() {
        try {
            Instant start = Instant.now();
            jsonLoggerService.newLogger(getClass()).put("short_message",
                "Iníciada a coleta de métricas do Cloudstack para o projeto de ID: " + projectId + " " + start + " ms")
                .sendInfo();
            Long timestamp = getTimestampToTelegraf();

            JsonNode loadBalances = getLoadBalances();

            Map<String, Map<String, String>> loadBalancerFormated = jsonNodeUtil.formmaterPostTelegraf(loadBalances);

            for (Entry<String, Map<String, String>> vip : loadBalancerFormated.entrySet()) {
                for (Entry<String, String> vm : vip.getValue().entrySet()) {

                    telegrafService.post(vm.getValue(), timestamp);

                    if (logger.isDebugEnabled()) {
                        jsonLoggerService.newLogger(getClass()).put("short_message", "Métrica: " + vm.getValue())
                            .sendDebug();
                    }
                }
            }

            Instant end = Instant.now();
            jsonLoggerService.newLogger(getClass()).put("short_message",
                "Finalizada a coleta de métricas do Cloudstack para o projeto de ID: " + projectId
                    + " Métricas enviadas em: " + (end.toEpochMilli() - start.toEpochMilli()) + "ms").sendInfo();
        } catch (Exception e) {
            jsonLoggerService.newLogger(getClass()).put("short_message", e.getMessage()).sendError();
        }
    }

    private long getTimestampToTelegraf() {
        return Calendar.getInstance().getTimeInMillis() * 1000L * 1000L;
    }


    private JsonNode getLoadBalances() throws Exception {
        JsonNode loadBalances = acsClientService.getLoadBalancesByProject();
        getDetailsLoadBalance(acsClientService, loadBalances);
        return loadBalances;
    }

    public void getDetailsLoadBalance(ACSClientService acsClientService, JsonNode loadBalances) {
        if (loadBalances != null && loadBalances.get("listloadbalancerrulesresponse") != null
            && loadBalances.get("listloadbalancerrulesresponse").get("loadbalancerrule") != null
            && loadBalances.get("listloadbalancerrulesresponse").get("loadbalancerrule").isArray()) {

            ExecutorService threadPool = Executors.newFixedThreadPool(
                loadBalances.get("listloadbalancerrulesresponse").get("loadbalancerrule").size());
            ExecutorCompletionService<JsonNode> completionService = new ExecutorCompletionService<>(threadPool);

            List<ACSCallable> asyncronousTasks = new ArrayList<>();

            for (JsonNode jsonNodeVIP : loadBalances.get("listloadbalancerrulesresponse").get("loadbalancerrule")) {
                if (jsonNodeVIP.size() != 0) {
                    asyncronousTasks.add(new ACSCallable(acsClientService, jsonNodeVIP));
                }

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
