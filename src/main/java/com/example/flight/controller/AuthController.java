package com.example.flight.controller;

import com.example.flight.service.AuthService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public String register(@RequestBody Map<String, String> body) {
        return authService.register(
            body.get("username"),
            body.get("password"),
            body.get("role")
        );
    }

    @PostMapping("/login")
    public String login(@RequestBody Map<String, String> body) {
        return authService.login(body.get("username"), body.get("password"));
    }
}