package com.expper.service.support;

import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;

import org.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;

/**
 * Hack RestTemplate call to get access to HttpHeaders
 *
 * @author kiranb
 */
public class BasicAuthRestTemplate extends RestTemplate {
    private HttpHeaders headers = new HttpHeaders();
    private final String username;
    private final String password;

    public BasicAuthRestTemplate(String username, String password) {
        super();
        this.username = username;
        this.password = password;
    }

    public HttpHeaders getRequestHttpHeaders() {
        addBasicAuthHeader();
        return headers;
    }

    private void addBasicAuthHeader() {
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes());
        String authHeaderStr = "Basic " + new String(encodedAuth);
        headers.set("Authorization", authHeaderStr);
        // TODO See if this is required for all the calls???
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    @Override
    protected <T> T doExecute(URI url, HttpMethod method,
                              RequestCallback requestCallback,
                              ResponseExtractor<T> responseExtractor) throws RestClientException {

        RequestCallbackDecorator requestCallbackDecorator = new RequestCallbackDecorator(
            requestCallback);

        return super.doExecute(url, method, requestCallbackDecorator,
            responseExtractor);
    }

    private class RequestCallbackDecorator implements RequestCallback {
        private RequestCallback targetRequestCallback;

        public RequestCallbackDecorator(RequestCallback targetRequestCallback) {
            this.targetRequestCallback = targetRequestCallback;
        }

        @Override
        public void doWithRequest(ClientHttpRequest request) throws IOException {
            request.getHeaders().putAll(getRequestHttpHeaders());

            if (null != targetRequestCallback) {
                targetRequestCallback.doWithRequest(request);
            }
        }
    }
}
