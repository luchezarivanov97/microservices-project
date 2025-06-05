package com.example.order_service.service;

import com.example.order_service.client.ProductInfoClient;
import com.example.order_service.client.UserInfoClient;
import com.example.order_service.event.OrderCreatedEvent;
import com.example.order_service.model.Order;
import com.example.order_service.model.ProductInfoResponse;
import com.example.order_service.model.UserInfoResponse;
import com.example.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserInfoClient userInfoClient;
    private final ProductInfoClient productInfoClient;
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public Order placeOrder(String token, String userEmail, List<Long> productIds) {
        // Validate user
        UserInfoResponse user = userInfoClient.getUserInfo(token);
        if (!user.getEmail().equals(userEmail)) {
            throw new RuntimeException("Email mismatch or unauthorized");
        }

        if (user.getRoles() == null || !user.getRoles().contains("ROLE_USER")) {
            throw new RuntimeException("Only users can place orders.");
        }

        // Validate products and calculate total
        double total = 0.0;
        List<Long> validProductIds = new ArrayList<>();

        for (Long productId : productIds) {
            try {
                ProductInfoResponse product = productInfoClient.getProductById(productId, token);
                if (product == null || product.getQuantity() <= 0) {
                    throw new RuntimeException("Product with ID " + productId + " is out of stock.");
                }
                productInfoClient.reduceProductQuantity(productId, 1, token); // update order to be able to order more than 1 item
                total += product.getPrice();
                validProductIds.add(productId);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to fetch product with ID " + productId + ": " + ex.getMessage());
            }
        }

        if (validProductIds.isEmpty()) {
            throw new RuntimeException("No valid products found in the request.");
        }
        // Save order
        Order order = new Order();
        order.setUserEmail(userEmail);
        order.setTotalPrice(total);
        order.setProductIds(validProductIds);

        Order savedOrder = orderRepository.save(order);

        // Send Kafka event
        OrderCreatedEvent event = new OrderCreatedEvent(
                savedOrder.getId(),
                savedOrder.getUserEmail(),
                savedOrder.getTotalPrice(),
                savedOrder.getProductIds()
        );
        kafkaTemplate.send("order-created-topic", event);
        return savedOrder;
    }

    public void deleteOrder(String token, Long orderId) {
        UserInfoResponse user = userInfoClient.getUserInfo(token);

        if (!user.getRoles().contains("ROLE_ADMIN")) {
            throw new RuntimeException("Only admins can delete orders");
        }

        if (!orderRepository.existsById(orderId)) {
            throw new RuntimeException("Order not found: ID " + orderId);
        }

        orderRepository.deleteById(orderId);
    }


    public List<Order> getAllOrdersForUser(String userEmail) {
        return orderRepository.findByUserEmail(userEmail);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}
