package com.example.product_service.controler;

import com.example.product_service.client.UserInfoClient;
import com.example.product_service.model.UserInfoResponse;
import com.example.product_service.model.Product;
import com.example.product_service.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final UserInfoClient userInfoClient;

    public ProductController(ProductService productService, UserInfoClient userInfoClient) {
        this.productService = productService;
        this.userInfoClient = userInfoClient;
    }

    private boolean isAdmin(UserInfoResponse userInfo) {
        if (userInfo == null || userInfo.getRoles() == null) {
            return false;
        }
        return userInfo.getRoles().contains("ROLE_ADMIN");
    }

    private UserInfoResponse getUserInfoFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        try {
            return userInfoClient.getUserInfo(token);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAll(@RequestHeader("Authorization") String authHeader) {
        getUserInfoFromToken(authHeader); // just to ensure token is valid
        return ResponseEntity.ok(productService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        getUserInfoFromToken(authHeader);
        Product product = productService.getById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }

    @PostMapping
    public ResponseEntity<Product> save(@Valid @RequestBody Product product, @RequestHeader("Authorization") String authHeader) {
        UserInfoResponse userInfo = getUserInfoFromToken(authHeader);
        if (!isAdmin(userInfo)) {
            return ResponseEntity.status(403).build();
        }
        Product saved = productService.save(product);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        UserInfoResponse userInfo = getUserInfoFromToken(authHeader);
        if (!isAdmin(userInfo)) {
            return ResponseEntity.status(403).build();
        }
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
