package com.globo.pepe.acscollector.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ACSCollectorConfiguration {

    private @Value("${telegraf.url}")
    String urlTelegraf;
    private @Value("${acs.url}")
    String urlACS;
    private @Value("${acs.api_key}")
    String apiKey;
    private @Value("${acs.secret_key}")
    String secretKey;
    private @Value("${acs.project_id}") String projectId;

    public String getUrlTelegraf() {
        return urlTelegraf;
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
