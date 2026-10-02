package com.example.flight_booking_service.controller;

import com.example.flight_booking_service.dto.MessageResponse;
import com.example.flight_booking_service.entity.Passenger;
import com.example.flight_booking_service.service.PassengerService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/passengers")
public class PassengerController {

    @Autowired
    private PassengerService passengerService;

    @PostMapping
    public Passenger createPassenger(
            @Valid @RequestBody Passenger passenger,
            Authentication authentication) {

        return passengerService.savePassenger(
                passenger,
                authentication.getName()
        );
    }

    @GetMapping
    public List<Passenger> getAllPassengers(
            Authentication authentication) {

        return passengerService.getAllPassengers(
                authentication.getName(),
                getRole(authentication)
        );
    }

    @PutMapping("/{id}")
    public Passenger updatePassenger(
            @PathVariable Long id,
            @Valid @RequestBody Passenger passenger,
            Authentication authentication) {

        return passengerService.updatePassenger(
                id,
                passenger,
                authentication.getName(),
                getRole(authentication)
        );
    }

    @DeleteMapping("/{id}")
    public MessageResponse deletePassenger(
            @PathVariable Long id,
            Authentication authentication) {

        passengerService.deletePassenger(
                id,
                authentication.getName(),
                getRole(authentication)
        );

        return new MessageResponse(
                "Passenger deleted successfully"
        );
    }

    private String getRole(Authentication authentication) {

        return authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER");
    }
}