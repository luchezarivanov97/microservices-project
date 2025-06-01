package com.example.product_service.client;

import com.example.product_service.model.UserInfoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

@Component
public class UserInfoClient {

    private final RestTemplate restTemplate;

    @Autowired
    public UserInfoClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public UserInfoResponse getUserInfo(String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<UserInfoResponse> response = restTemplate.exchange(
                    "http://localhost:8080/api/auth/user-info",
                    HttpMethod.GET,
                    entity,
                    UserInfoResponse.class
            );

            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            return null;
        }
    }
}
