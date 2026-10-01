package com.example.flight_booking_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI bookingServiceOpenAPI() {

        return new OpenAPI()
                .info(
                        new Info()
                                .title("Flight Booking Service API")
                                .description(
                                        "Microservice responsible for "
                                        + "passengers and flight bookings"
                                )
                                .version("1.0.0")
                );
    }
}