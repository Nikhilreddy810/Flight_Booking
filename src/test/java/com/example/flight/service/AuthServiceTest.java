package com.example.flight.service;

import com.example.flight.entity.User;
import com.example.flight.exception.InvalidCredentialsException;
import com.example.flight.exception.UserAlreadyExistsException;
import com.example.flight.repository.UserRepository;
import com.example.flight.security.JwtUtil;
import com.example.flight.security.Roles;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User storedUser(String role) {
        User user = new User();
        user.setUsername("nikhil");
        user.setPassword("hashed");
        user.setRole(role);
        return user;
    }

    @Test
    void shouldAlwaysRegisterAsPlainUser() {
        when(userRepository.findByUsername("nikhil")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass123")).thenReturn("hashed");

        authService.register("nikhil", "pass123");

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertEquals(Roles.USER, saved.getValue().getRole(),
                "self-registration must never mint an admin");
        assertEquals("hashed", saved.getValue().getPassword(),
                "the raw password must never be stored");
    }

    @Test
    void shouldRejectDuplicateUsername() {
        when(userRepository.findByUsername("nikhil")).thenReturn(Optional.of(storedUser(Roles.USER)));

        assertThrows(UserAlreadyExistsException.class, () -> {
            authService.register("nikhil", "pass123");
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldReturnToken_onSuccessfulLogin() {
        when(userRepository.findByUsername("nikhil")).thenReturn(Optional.of(storedUser(Roles.USER)));
        when(passwordEncoder.matches("pass123", "hashed")).thenReturn(true);
        when(jwtUtil.generateToken("nikhil", Roles.USER)).thenReturn("a.b.c");

        assertEquals("a.b.c", authService.login("nikhil", "pass123"));
    }

    @Test
    void shouldRejectWrongPassword() {
        when(userRepository.findByUsername("nikhil")).thenReturn(Optional.of(storedUser(Roles.USER)));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> {
            authService.login("nikhil", "wrong");
        });

        verify(jwtUtil, never()).generateToken(anyString(), anyString());
    }

    @Test
    void shouldRejectUnknownUser() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> {
            authService.login("ghost", "pass123");
        });
    }
}
