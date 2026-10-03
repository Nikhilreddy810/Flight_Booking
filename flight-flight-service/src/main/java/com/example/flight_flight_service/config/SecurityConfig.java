package com.example.flight_flight_service.config;

import com.example.flight_flight_service.security.JwtFilter;
import com.example.flight_flight_service.security.RestAccessDeniedHandler;
import com.example.flight_flight_service.security.RestAuthenticationEntryPoint;

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

    @Autowired
    private RestAuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    private RestAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .exceptionHandling(ex ->
                        ex
                                .authenticationEntryPoint(
                                        authenticationEntryPoint
                                )
                                .accessDeniedHandler(
                                        accessDeniedHandler
                                )
                )

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/error"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/flights/**"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/flights"
                        ).hasAuthority("ROLE_ADMIN")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/flights/*/reserve-seat"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/flights/*/release-seat"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/flights/**"
                        ).hasAuthority("ROLE_ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/flights/**"
                        ).hasAuthority("ROLE_ADMIN")

                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}