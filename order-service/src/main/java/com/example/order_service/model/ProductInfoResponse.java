package com.example.order_service.model;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductInfoResponse {
    private Long id;
    private String name;
    private Double price;
    private Integer quantity;
}