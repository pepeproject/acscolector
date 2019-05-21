package com.globo.pepe.acscollector.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class ACSClientService {

    @Value("${acs.project_id}")
    private String projectId;

    @Value("${acs.url}")
    private String urlACS;

    @Value("${acs.api_key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final HmacUtils hmacUtils;

    public ACSClientService(RestTemplate restTemplate, @Value("${acs.secret_key}") String secretKey) {
        this.restTemplate = restTemplate;
        this.hmacUtils = new HmacUtils(HmacAlgorithms.HMAC_SHA_1, secretKey);
    }

    public JsonNode getLoadBalanceInstances(String loadBalancerId) throws Exception {
        Map<String,String> params = makeParamsLoadBalance("listLoadBalancerRuleInstances",loadBalancerId);
        return executeACScommand(params, "listLoadBalancerRuleInstances");
    }

    public JsonNode getLoadBalancesByProject() throws Exception {
        Map<String,String> params = makeParamsLoadBalance("listLoadBalancerRules",null);
        return executeACScommand(params, "listLoadBalancerRules");
    }

    public JsonNode getAutoScaleByLB(String loadBalanceId) throws Exception {
        Map<String,String> params = makeParamsLoadBalance("listAutoScaleVmGroups",loadBalanceId);
        return executeACScommand(params, "listAutoScaleVmGroups");
    }

    protected JsonNode executeACScommand(Map<String,String> params, String consulta) throws Exception {
        MultiValueMap<String, String> paramsMulti = new LinkedMultiValueMap<>();
        paramsMulti.setAll(params);
        String url = uriBuilder(URI.create(urlACS), paramsMulti);
        JsonNode response = null;
        response = restTemplate.getForObject(url, JsonNode.class);
        System.out.println("URL "+url+" response "+response + " consulta "+consulta);

        return response;
    }

    public String uriBuilder(final URI uri, final MultiValueMap<String, String> params) {
        return UriComponentsBuilder.fromUri(uri).queryParams(params).build().toUriString();
    }

    private String getSignature(Map<String, String> params) throws UnsupportedEncodingException {
        List<String> listParams = new LinkedList<>();
        for (Entry<String, String> entry : params.entrySet()) {
                listParams.add(entry.getKey().toLowerCase() + "=" + URLEncoder.encode(
                    entry.getValue().toLowerCase().replaceAll("\\+", "%20"), "UTF-8"));
        }

        Collections.sort(listParams);
        String queryString = String.join("&", listParams);
        byte[] signatureBytes = hmacUtils.hmac(queryString.toLowerCase());

        return URLEncoder.encode(Base64.encodeBase64String(signatureBytes), "UTF-8");
    }

    public Map<String, String> makeParamsLoadBalance(String command,String loadBalancerId) throws UnsupportedEncodingException {
        Map<String, String> params = new LinkedHashMap<>();

        params.put("apiKey", apiKey);
        params.put("command",command);


        params.put("expires", "2019-05-21T11:59:13-0300");
        params.put("listAll", "true");


        if(command.equalsIgnoreCase("listAutoScaleVmGroups") && loadBalancerId != null){
            params.put("lbruleid",loadBalancerId);
            params.put("projectid", projectId);
        }

        if(command.equalsIgnoreCase("listLoadBalancerRuleInstances") && loadBalancerId != null){
            params.put("id",loadBalancerId);
            params.put("projectid", projectId);
        }

        if(command.equalsIgnoreCase("listLoadBalancerRules")){
            params.put("projectid", projectId);
        }

        params.put("response", "json");
        params.put("signatureVersion", "3");
        params.put("signature", getSignature(params).trim());

        return params;
    }




}
