package com.globo.pepe.acscollector.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ACSCollectorConfiguration {

    private @Value("${url_sofia}")
    String urlSofia;
    private @Value("${url_acs}")
    String urlACS;
    private @Value("${acs_api_key}")
    String apiKey;
    private @Value("${acs_secret_key}")
    String secretKey;
    private @Value("${acs_project_id}") String projectId;

    public String getUrlSofia() {
        return urlSofia;
    }


    public String getApiKey() {
        return apiKey;
    }


    public String getSecretKey() {
        return secretKey;
    }


    public String getUrlACS() {
        return urlACS;
    }


    public String getProjectId() {
        return projectId;
    }

}
