package com.example.flight_booking_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${flight.service.url}")
    private String flightServiceUrl;

    @Bean
    public RestClient flightServiceRestClient() {

        return RestClient.builder()
                .baseUrl(flightServiceUrl)
                .requestInterceptor((request, body, execution) -> {

                    Authentication authentication =
                            SecurityContextHolder
                                    .getContext()
                                    .getAuthentication();

                    if (authentication != null
                            && authentication.getCredentials() instanceof String token) {

                        request.getHeaders()
                                .setBearerAuth(token);
                    }

                    return execution.execute(
                            request,
                            body
                    );
                })
                .build();
    }
}