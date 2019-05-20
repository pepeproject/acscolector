package com.globo.pepe.acscollector.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javafx.collections.transformation.SortedList;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.DefaultUriBuilderFactory.EncodingMode;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class ACSClientService {


    private @Value("${acs.project_id}") String projectId;
    private @Value("${acs.url}") String urlACS;
    private @Value("${acs.api_key}") String apiKey;
    private @Value("${acs.secret_key}") String secretKey;

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    private  RestTemplate restTemplate;
    private ObjectMapper mapper;

    public ACSClientService() {
        this.mapper = new ObjectMapper();
    }


    public JsonNode getLoadBalanceInstances(String loadBalancerId) throws Exception {
        Map<String,String> params = makeParamsLoadBalance("listLoadBalancerRuleInstances",loadBalancerId);
        return executeACScommand(params);
    }

    public JsonNode getLoadBalancesByProject() throws Exception {
        Map<String,String> params = makeParamsLoadBalance("listLoadBalancerRules",null);
        return executeACScommand(params);
    }

    public JsonNode getAutoScaleByLB(String loadBalanceId) throws Exception {
        Map<String,String> params = makeParamsLoadBalance("listAutoScaleVmGroups",loadBalanceId);
        return executeACScommand(params);
    }

    protected JsonNode executeACScommand(Map<String,String> params) throws Exception {
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory();
        factory.setEncodingMode(EncodingMode.VALUES_ONLY);
        restTemplateBuilder = restTemplateBuilder.uriTemplateHandler(factory);
        this.restTemplate = restTemplateBuilder.build();

        MultiValueMap<String,String> paramsMulti =  new LinkedMultiValueMap<>();
        paramsMulti.setAll(params);
        String url = uriBuilder(URI.create(urlACS),paramsMulti);
        System.out.println(url);
        String response = restTemplate.getForObject(url,String.class);
        return  mapper.readTree(response);
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
        byte[] signatureBytes = new HmacUtils(HmacAlgorithms.HMAC_SHA_1, secretKey)
            .hmac(queryString.toLowerCase());
        String signature = URLEncoder.encode(Base64.encodeBase64String(signatureBytes), "UTF-8");

        return signature;
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
