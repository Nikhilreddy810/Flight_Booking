package com.example.flight_flight_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI flightServiceOpenAPI() {

        return new OpenAPI()
                .info(
                        new Info()
                                .title("Flight Service API")
                                .description(
                                        "Microservice responsible for "
                                        + "flight inventory and seat management"
                                )
                                .version("1.0.0")
                );
    }
}