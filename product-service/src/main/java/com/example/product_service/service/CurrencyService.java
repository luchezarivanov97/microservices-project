package com.example.product_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class CurrencyService {

    @Value("${currency.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public CurrencyService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Double convertToBGN(String fromCurrency, Double amount) {
        String url = "https://api.apilayer.com/exchangerates_data/convert?to=BGN&from="
                + fromCurrency + "&amount=" + amount;

        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", apiKey);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                Map.class
        );

        Map<String, Object> body = response.getBody();

        if (body != null && body.containsKey("result")) {
            return Double.parseDouble(body.get("result").toString());
        } else {
            throw new RuntimeException("Failed to convert currency: " + fromCurrency);
        }
    }

    public Map<String, Double> convertAllToBGN(Map<String, Double> priceMap) {
        if (priceMap == null || priceMap.isEmpty()) {
            return Collections.emptyMap();
        }

        // New map to hold original + BGN conversions
        Map<String, Double> resultMap = new HashMap<>(priceMap);

        // If the map already contains a "BGN" key, don't convert
        if (!resultMap.containsKey("BGN")) {
            for (Map.Entry<String, Double> entry : priceMap.entrySet()) {
                String currency = entry.getKey();
                Double amount = entry.getValue();

                if (!"BGN".equalsIgnoreCase(currency)) {
                    Double converted = convertToBGN(currency, amount);
                    resultMap.put("BGN", converted);
                    break;
                }
            }
        }

        return resultMap;
    }
}


