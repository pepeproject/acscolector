package com.globo.pepe.acscollector.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.concurrent.Callable;

public class ExemploCallable implements Callable<JsonNode> {

    private final long tempoDeEspera;
    private final ACSClient acsClient;
    private final String id;

    public ExemploCallable(int time,ACSClient acsClient,String id) {
        this.tempoDeEspera = time;
        this.acsClient = acsClient;
        this.id = id;

    }

    @Override
    public JsonNode call() throws Exception {

        return acsClient.getLoadBalanceInstances(id);
    }

}
