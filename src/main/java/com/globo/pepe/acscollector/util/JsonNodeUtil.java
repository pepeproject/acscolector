package com.globo.pepe.acscollector.util;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonNodeUtil {

    public static JsonNode desirializerJsonNode(String json)throws  Exception{
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        objectMapper.setSerializationInclusion(Include.NON_EMPTY);
        JsonNode node = objectMapper.readTree(json);
        return  node;

    }
}
