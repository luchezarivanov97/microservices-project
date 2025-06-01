package com.example.userLogin.controller;

import com.example.userLogin.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthInternalController {

    @Value("${service.secret-token}")
    private String secretToken;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/user-info")
    public ResponseEntity<?> getUserInfo(
            @RequestParam String email,
            @RequestHeader("Authorization") String authHeader) {

        if (!authHeader.equals("Bearer " + secretToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized service request");
        }

        return userRepository.findByEmail(email)
                .map(user -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("email", user.getEmail());
                    response.put("roles", user.getRoles());
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
