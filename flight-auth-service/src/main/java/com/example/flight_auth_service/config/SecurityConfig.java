package com.example.flight_auth_service.config;

import com.example.flight_auth_service.repository.UserRepository;
import com.example.flight_auth_service.security.JwtFilter;
import com.example.flight_auth_service.security.RestAccessDeniedHandler;
import com.example.flight_auth_service.security.RestAuthenticationEntryPoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestAuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    private RestAccessDeniedHandler accessDeniedHandler;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {

            com.example.flight_auth_service.entity.User user =
                    userRepository.findByUsername(username)
                            .orElseThrow(() ->
                                    new UsernameNotFoundException("User not found"));

            return new org.springframework.security.core.userdetails.User(
                    user.getUsername(),
                    user.getPassword(),
                    List.of(
                            new SimpleGrantedAuthority(user.getRole())
                    )
            );
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            // Disable CSRF because this is a stateless REST API
            .csrf(csrf -> csrf.disable())

            // JWT authentication → no server-side sessions
            .sessionManagement(session ->
                    session.sessionCreationPolicy(
                            SessionCreationPolicy.STATELESS
                    )
            )

            // Custom REST authentication/authorization error handlers
            .exceptionHandling(ex ->
                    ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
            )

            // Disable request cache
            .requestCache(requestCache ->
                    requestCache.disable()
            )

            // Authorization rules
            .authorizeHttpRequests(auth -> auth

                    // Swagger / OpenAPI
                    .requestMatchers(
                            "/swagger-ui.html",
                            "/swagger-ui/**",
                            "/v3/api-docs",
                            "/v3/api-docs/**",
                            "/webjars/**",
                            "/error"
                    ).permitAll()

                    // Authentication endpoints
                    .requestMatchers("/api/auth/**")
                    .permitAll()

                    // Everything else requires JWT
                    .anyRequest()
                    .authenticated()
            )

            // JWT filter
            .addFilterBefore(
                    jwtFilter,
                    UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}