package org.example.hbparser.service.util;

import org.example.hbparser.security.HBAuthorization;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;

public class HttpHeaderUtil {

    public static HttpEntity<String> createHttpEntityWithHeaders(HBAuthorization apiClient) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.set("Api-key", apiClient.getApiKey());
        headers.set("X-Signature", apiClient.getXSignature());
        return new HttpEntity<>(headers);
    }
}
