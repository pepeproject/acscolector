package com.globo.pepe.acscollector.util;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class JsonNodeUtil {

    public   Map<String, Map<String,String>> formmaterPostTelegraf(JsonNode loadbalances) {
            Map<String, Map<String, String>> loadbalancesToTelegrafMetric = new LinkedHashMap<String, Map<String, String>>();
            Map<String, String> virtualMachines = new LinkedHashMap<String, String>();
            if (loadbalances != null && loadbalances.get("listloadbalancerrulesresponse") != null) {
                for (JsonNode loadBalance : loadbalances.get("listloadbalancerrulesresponse").get("loadbalancerrule")) {
                    if(loadBalance.get("virtualMachines") != null){
                        virtualMachines = new LinkedHashMap<String, String>();
                        for (JsonNode virtualMachine : loadBalance.get("virtualMachines")) {
                            virtualMachines.put(virtualMachine.get("id").asText(),buildVirtualMachineIdToTelegrafMetric(loadBalance, virtualMachine));
                        }
                    }
                    loadbalancesToTelegrafMetric.put(loadBalance.get("name").asText(), virtualMachines);
                }
            }
            return loadbalancesToTelegrafMetric;
    }

    private  String buildVirtualMachineIdToTelegrafMetric(JsonNode loadBalance, JsonNode virtualMachine) {
        StringBuilder metricTelegraf = new StringBuilder("pepe_acs_metrics");

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
        }else{
            metricTelegraf.append("autoscale_minmembers=").append("0");
        }

        if(loadBalance.get("autoScaleGroup") != null && loadBalance.get("autoScaleGroup").isArray() && loadBalance.get("autoScaleGroup").get(0).get("maxmembers") != null){
            metricTelegraf.append(",autoscale_maxmembers=").append(loadBalance.get("autoScaleGroup").get(0).get("maxmembers").asText());

        }else{
            metricTelegraf.append(",autoscale_maxmembers=").append("0");
        }

        if(loadBalance.get("autoScaleGroup") != null && loadBalance.get("autoScaleGroup").isArray() && loadBalance.get("autoScaleGroup").get(0).get("autoscalegroupcountmembers") != null){
            metricTelegraf.append(",autoscale_count=").append(loadBalance.get("autoScaleGroup").get(0).get("autoscalegroupcountmembers").asText());
        }else{
            metricTelegraf.append(",autoscale_count=").append("0");
        }

        return metricTelegraf.toString();
    }
}
