package com.example.order_service.model;

import lombok.*;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductInfoResponse {
    private Long id;
    private String name;
    private Map<String, Double> price;
    private Integer quantity;
}