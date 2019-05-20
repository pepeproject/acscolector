package com.globo.pepe.acscollector.util;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.LinkedHashMap;
import java.util.Map;

public class JsonNodeUtil {

    public static  Map<String, Map<String,String>> formmaterPostTelegraf(JsonNode loadbalances) {
            Map<String, Map<String, String>> loadbalancesToTelegrafMetric = new LinkedHashMap<String, Map<String, String>>();
            Map<String, String> virtualMachines = new LinkedHashMap<String, String>();
            if (loadbalances != null && loadbalances.get("listloadbalancerrulesresponse") != null) {
                for (JsonNode loadBalance : loadbalances.get("listloadbalancerrulesresponse").get("loadbalancerrule")) {
                    if(loadBalance.get("virtualMachines") != null){
                        for (JsonNode virtualMachine : loadBalance.get("virtualMachines")) {
                            virtualMachines = buildVirtualMachineIdToTelegrafMetric(loadBalance, virtualMachine);
                        }
                    }
                    loadbalancesToTelegrafMetric.put(loadBalance.get("name").asText(), virtualMachines);
                }
            }
            return loadbalancesToTelegrafMetric;
    }

    private static Map<String, String> buildVirtualMachineIdToTelegrafMetric(JsonNode loadBalance, JsonNode virtualMachine) {
        Map<String, String> virtualMachines;
        StringBuilder metricTelegraf = new StringBuilder("pepe_acs_metrics");
        virtualMachines = new LinkedHashMap<String, String>();

        if(loadBalance.get("id") != null){
            metricTelegraf.append(",vip_id=").append(loadBalance.get("id").asText());
        }

        if(loadBalance.get("name") != null){
            metricTelegraf.append(",vip_name=").append(loadBalance.get("name").asText());
        }

        if(virtualMachine.get("name") != null){
            metricTelegraf.append(",vm_name=").append(virtualMachine.get("name").asText());
        }

        if(virtualMachine.get("id") != null){
           metricTelegraf.append(",vm_id=").append(virtualMachine.get("id").asText());
        }

        if(virtualMachine.get("project") != null){
            metricTelegraf.append(",vm_project=").append(virtualMachine.get("project").asText().replaceAll(" ", "\\\\ "));
        }

        if(virtualMachine.get("projectid") != null){
            metricTelegraf.append(",project_id=").append(virtualMachine.get("projectid").asText());
        }

        if(virtualMachine.get("state") != null){
           metricTelegraf.append(",vm_state=").append(virtualMachine.get("state").asText());
        }

        if(virtualMachine.get("created") != null){
            metricTelegraf.append(",vm_created=").append(virtualMachine.get("created").asText());
        }

        if(virtualMachine.get("nic") != null && virtualMachine.get("nic").isArray() && virtualMachine.get("nic").get(0).get("ipaddress") != null){
            metricTelegraf.append(",vm_ip_address=").append(virtualMachine.get("nic").get(0).get("ipaddress").asText());
        }
        
       metricTelegraf.append(" ");
        
        if(loadBalance.get("autoScaleGroup") != null && loadBalance.get("autoScaleGroup").isArray() && loadBalance.get("autoScaleGroup").get(0).get("minmembers") != null){
            metricTelegraf.append("autoscale_minmembers=").append(loadBalance.get("autoScaleGroup").get(0).get("minmembers").asText());

        }

        if(loadBalance.get("autoScaleGroup") != null && loadBalance.get("autoScaleGroup").isArray() && loadBalance.get("autoScaleGroup").get(0).get("maxmembers") != null){
            metricTelegraf.append(",autoscale_maxmembers=").append(loadBalance.get("autoScaleGroup").get(0).get("maxmembers").asText());

        }

        if(loadBalance.get("autoScaleGroup") != null && loadBalance.get("autoScaleGroup").isArray() && loadBalance.get("autoScaleGroup").get(0).get("autoscalegroupcountmembers") != null){
            metricTelegraf.append(",autoscale_count=").append(loadBalance.get("autoScaleGroup").get(0).get("autoscalegroupcountmembers").asText());
        }

        virtualMachines.put(virtualMachine.get("id").asText(), metricTelegraf.toString());
        return virtualMachines;
    }
}
