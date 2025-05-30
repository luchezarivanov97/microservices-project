package com.example.userLogin.service;

import com.example.userLogin.dto.UserLoginRequest;
import com.example.userLogin.dto.UserRegisterRequest;
import com.example.userLogin.model.User;
import com.example.userLogin.repository.UserRepository;
import com.example.userLogin.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    public String register(UserRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return "User with this email already exists.";
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(request.getEmail(), hashedPassword);
        userRepository.save(user);
        return "User registered successfully.";
    }

    public String login(UserLoginRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return jwtService.generateToken(user.getEmail());
            }
        }

        throw new RuntimeException("Invalid email or password");
    }
}
