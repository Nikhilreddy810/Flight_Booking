package com.example.flight;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class FlightBookingApplication {
    public static void main(String[] args) {
        SpringApplication.run(FlightBookingApplication.class, args);
    }
}