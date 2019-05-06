package com.globo.pepe.acscollector.service;

import java.io.IOException;

import org.springframework.http.HttpStatus.Series;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;

@Component
public class RestTemplateResponseErrorHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return (response.getStatusCode().series() == Series.CLIENT_ERROR
                || response.getStatusCode().series() == Series.SERVER_ERROR);
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        if (response.getStatusCode().series() == Series.SERVER_ERROR
                || response.getStatusCode().series() == Series.CLIENT_ERROR) {
            throw new IOException("Response status: " + response.getStatusCode());
        }
    }

}
