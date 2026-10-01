package com.example.flight_auth_service.service;

import com.example.flight_auth_service.entity.User;
import com.example.flight_auth_service.exception.InvalidCredentialsException;
import com.example.flight_auth_service.exception.UserAlreadyExistsException;
import com.example.flight_auth_service.repository.UserRepository;
import com.example.flight_auth_service.security.JwtUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();

        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setRole("ROLE_USER");
    }

    @Test
    void register_success() {

        when(userRepository.findByUsername("newuser"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String result = authService.register(
                "newuser",
                "password123"
        );

        assertEquals(
                "User registered successfully",
                result
        );

        verify(userRepository)
                .findByUsername("newuser");

        verify(passwordEncoder)
                .encode("password123");

        verify(userRepository)
                .save(any(User.class));
    }

    @Test
    void register_duplicateUsername_throwsException() {

        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(user));

        assertThrows(
                UserAlreadyExistsException.class,
                () -> authService.register(
                        "testuser",
                        "password123"
                )
        );

        verify(userRepository)
                .findByUsername("testuser");

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void login_success() {

        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "password123",
                "encodedPassword"
        )).thenReturn(true);

        when(jwtUtil.generateToken(
                "testuser",
                "ROLE_USER"
        )).thenReturn("jwt-token");

        String token = authService.login(
                "testuser",
                "password123"
        );

        assertEquals(
                "jwt-token",
                token
        );

        verify(userRepository)
                .findByUsername("testuser");

        verify(passwordEncoder)
                .matches(
                        "password123",
                        "encodedPassword"
                );

        verify(jwtUtil)
                .generateToken(
                        "testuser",
                        "ROLE_USER"
                );
    }

    @Test
    void login_userNotFound_throwsException() {

        when(userRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(
                        "unknown",
                        "password123"
                )
        );

        verify(userRepository)
                .findByUsername("unknown");

        verify(jwtUtil, never())
                .generateToken(any(), any());
    }

    @Test
    void login_wrongPassword_throwsException() {

        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "wrongPassword",
                "encodedPassword"
        )).thenReturn(false);

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(
                        "testuser",
                        "wrongPassword"
                )
        );

        verify(passwordEncoder)
                .matches(
                        "wrongPassword",
                        "encodedPassword"
                );

        verify(jwtUtil, never())
                .generateToken(any(), any());
    }
}