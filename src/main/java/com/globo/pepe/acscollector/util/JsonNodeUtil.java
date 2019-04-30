package com.globo.pepe.acscollector.util;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.core.env.SystemEnvironmentPropertySource;

public class JsonNodeUtil {

    public static JsonNode desirializerJsonNode(String json)throws  Exception{
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        objectMapper.setSerializationInclusion(Include.NON_EMPTY);
        JsonNode node = objectMapper.readTree(json);
        return  node;

    }

    public static  Map<String, Map<String,String>> formmaterPostTelegraf(JsonNode loadbalance){
            Map<String, Map<String, String>> loadbalances = new LinkedHashMap<String, Map<String, String>>();
            Map<String, String> virtualMachines = new LinkedHashMap<String, String>();
            if (loadbalance != null && loadbalance.get("listloadbalancerrulesresponse") != null) {
                for (JsonNode loadBalance : loadbalance.get("listloadbalancerrulesresponse").get("loadbalancerrule")) {
                    if(loadBalance.get("virtualMachines") != null){
                        for (JsonNode virtualMachine : loadBalance.get("virtualMachines")) {
                            String metric = "pepe_acs_metrics";
                            virtualMachines = new LinkedHashMap<String, String>();

                            if(loadBalance.get("id") != null){
                                metric = metric.concat(",vip_id=").concat(loadBalance.get("id").asText());
                            }

                            if(loadBalance.get("name") != null){
                                metric = metric.concat(",vip_name=").concat(loadBalance.get("name").asText());
                            }

                            if(virtualMachine.get("name") != null){
                                metric = metric.concat(",vm_name=").concat(virtualMachine.get("name").asText());
                            }

                            if(virtualMachine.get("id") != null){
                                metric = metric.concat(",vm_id=").concat(virtualMachine.get("id").asText());
                            }

                            if(virtualMachine.get("project") != null){
                                metric = metric.concat(",vm_project=").concat(virtualMachine.get("project").asText().replaceAll(" ", "\\\\ "));
                            }

                            if(virtualMachine.get("projectid") != null){
                                metric = metric.concat(",project_id=").concat(virtualMachine.get("projectid").asText());
                            }

                            if(virtualMachine.get("state") != null){
                                metric = metric.concat(",vm_state=").concat(virtualMachine.get("state").asText());
                            }

                            if(virtualMachine.get("created") != null){
                                metric = metric.concat(",vm_created=").concat(virtualMachine.get("created").asText());
                            }

                            if(virtualMachine.get("nic") != null && virtualMachine.get("nic").isArray() && virtualMachine.get("nic").get(0).get("ipaddress") != null){
                                metric = metric.concat(",vm_ip_address=").concat(virtualMachine.get("nic").get(0).get("ipaddress").asText());
                            }
                            metric = metric.concat(" ");
                            if(loadBalance.get("autoScaleGroup") != null && loadBalance.get("autoScaleGroup").isArray() && loadBalance.get("autoScaleGroup").get(0).get("minmembers") != null){
                                metric = metric.concat("autoscale_minmembers=").concat(loadBalance.get("autoScaleGroup").get(0).get("minmembers").asText());

                            }

                            if(loadBalance.get("autoScaleGroup") != null && loadBalance.get("autoScaleGroup").isArray() && loadBalance.get("autoScaleGroup").get(0).get("maxmembers") != null){
                                metric = metric.concat(",autoscale_maxmembers=").concat(loadBalance.get("autoScaleGroup").get(0).get("maxmembers").asText());

                            }

                            if(loadBalance.get("autoScaleGroup") != null && loadBalance.get("autoScaleGroup").isArray() && loadBalance.get("autoScaleGroup").get(0).get("autoscalegroupcountmembers") != null){
                                metric = metric.concat(",autoscale_count=").concat(loadBalance.get("autoScaleGroup").get(0).get("autoscalegroupcountmembers").asText());
                            }

                            virtualMachines.put(virtualMachine.get("id").asText(), metric);
                        }
                    }
                    loadbalances.put(loadBalance.get("name").asText(), virtualMachines);
                }
            }
            return loadbalances;
    }




}
