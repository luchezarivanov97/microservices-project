package com.example.order_service.controller;

import com.example.order_service.client.UserInfoClient;
import com.example.order_service.model.Order;
import com.example.order_service.model.OrderRequest;
import com.example.order_service.model.UserInfoResponse;
import com.example.order_service.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserInfoClient userInfoClient;

    public OrderController(OrderService orderService, UserInfoClient userInfoClient) {
        this.orderService = orderService;
        this.userInfoClient = userInfoClient;
    }

    private UserInfoResponse getUserInfoFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        return userInfoClient.getUserInfo(token);
    }

    @PostMapping
    public ResponseEntity<Order> placeOrder(@RequestBody OrderRequest request,
                                            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        UserInfoResponse userInfo = userInfoClient.getUserInfo(token);

        Order order = orderService.placeOrder(token, userInfo.getEmail(), request.getProductIds());
        return ResponseEntity.ok(order);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<String> deleteOrder(
            @RequestHeader("Authorization") String token,
            @PathVariable Long orderId) {

        String cleanedToken = token.replace("Bearer ", "");
        orderService.deleteOrder(cleanedToken, orderId);
        return ResponseEntity.ok("Order deleted successfully");
    }

    @GetMapping
    public ResponseEntity<List<Order>> getUserOrders(@RequestHeader("Authorization") String authHeader) {
        UserInfoResponse userInfo = getUserInfoFromToken(authHeader);
        List<Order> orders = orderService.getAllOrdersForUser(userInfo.getEmail());
        return ResponseEntity.ok(orders);
    }
}
