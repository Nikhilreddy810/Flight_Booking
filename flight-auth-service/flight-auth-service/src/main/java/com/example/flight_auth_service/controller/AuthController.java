package com.example.flight_auth_service.controller;

import com.example.flight_auth_service.dto.AuthResponse;
import com.example.flight_auth_service.dto.LoginRequest;
import com.example.flight_auth_service.dto.RegisterRequest;
import com.example.flight_auth_service.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public String register(
            @Valid @RequestBody RegisterRequest request) {

        return authService.register(
                request.getUsername(),
                request.getPassword()
        );
    }

    @PostMapping("/login")
    public AuthResponse login(
            @Valid @RequestBody LoginRequest request) {

        return new AuthResponse(
                authService.login(
                        request.getUsername(),
                        request.getPassword()
                )
        );
    }
}