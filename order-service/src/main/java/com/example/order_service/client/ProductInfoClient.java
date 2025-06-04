package com.example.order_service.client;

import com.example.order_service.model.ProductInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class ProductInfoClient {

    private final RestTemplate restTemplate;

    public ProductInfoResponse getProductById(Long productId, String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<ProductInfoResponse> response = restTemplate.exchange(
                    "http://localhost:8081/api/products/" + productId,
                    HttpMethod.GET,
                    entity,
                    ProductInfoResponse.class
            );

            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            throw new RuntimeException("Product not found");
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Failed to fetch product: " + e.getMessage());
        }
    }


    public void reduceProductQuantity(Long productId, int quantity, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String url = "http://localhost:8081/api/products/reduceQuantity/" + productId + "?quantity=" + quantity;
        try {
            restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Failed to reduce product quantity: " + e.getMessage());
        }
    }

}
