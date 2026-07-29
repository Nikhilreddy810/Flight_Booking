package com.example.flight.controller;

import com.example.flight.dto.AuthResponse;
import com.example.flight.dto.LoginRequest;
import com.example.flight.dto.MessageResponse;
import com.example.flight.dto.RegisterRequest;
import com.example.flight.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public MessageResponse register(@Valid @RequestBody RegisterRequest request) {
        return new MessageResponse(
            authService.register(request.getUsername(), request.getPassword()));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return new AuthResponse(
            authService.login(request.getUsername(), request.getPassword()));
    }
}
