package com.example.flight_flight_service.config;

import com.example.flight_flight_service.security.JwtFilter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // Swagger
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // Anyone can view flights
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/flights/**"
                        ).permitAll()

                        // Only ADMIN can create flights
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/flights"
                        ).hasAuthority("ROLE_ADMIN")

                        // Authenticated users can reserve seats
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/flights/*/reserve-seat"
                        ).authenticated()

                        // Authenticated users can release seats
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/flights/*/release-seat"
                        ).authenticated()

                        // Only ADMIN can update flights
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/flights/**"
                        ).hasAuthority("ROLE_ADMIN")

                        // Only ADMIN can delete flights
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/flights/**"
                        ).hasAuthority("ROLE_ADMIN")

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}