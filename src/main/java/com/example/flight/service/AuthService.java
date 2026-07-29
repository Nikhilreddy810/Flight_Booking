package com.example.flight.service;

import com.example.flight.entity.User;
import com.example.flight.exception.InvalidCredentialsException;
import com.example.flight.exception.UserAlreadyExistsException;
import com.example.flight.repository.UserRepository;
import com.example.flight.security.JwtUtil;
import com.example.flight.security.Roles;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Public self-registration. The role is never taken from the request body —
     * every account created here is a plain ROLE_USER. Admin accounts are
     * provisioned by AdminSeeder from configuration instead.
     */
    public String register(String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new UserAlreadyExistsException("Username already exists");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Roles.USER);
        userRepository.save(user);
        return "User registered successfully";
    }

    public String login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        return jwtUtil.generateToken(username, user.getRole());
    }
}
