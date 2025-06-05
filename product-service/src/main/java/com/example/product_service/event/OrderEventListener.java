package com.example.product_service.event;

import com.example.product_service.repository.ProductRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderEventListener {

    private final ProductRepository productRepository;

    public OrderEventListener(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @KafkaListener(topics = "order-created", groupId = "product-group", containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void handleOrderCreated(OrderCreatedEvent event) {
        for (Long productId : event.getProductIds()) {
            productRepository.findById(productId).ifPresent(product -> {
                if (product.getQuantity() > 0) {
                    product.setQuantity(product.getQuantity() - 1); // Reduce by 1 per product
                    productRepository.save(product);
                }
            });
        }

        System.out.println("Updated product quantities for order: " + event.getOrderId());
    }
}
