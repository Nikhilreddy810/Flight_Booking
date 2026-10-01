package com.example.flight_booking_service.controller;

import com.example.flight_booking_service.dto.MessageResponse;
import com.example.flight_booking_service.entity.Passenger;
import com.example.flight_booking_service.service.PassengerService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
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
            @RequestHeader("X-Username") String username) {

        return passengerService.savePassenger(passenger, username);
    }

    @GetMapping
    public List<Passenger> getAllPassengers(
            @RequestHeader("X-Username") String username,
            @RequestHeader(value = "X-Role", defaultValue = "ROLE_USER") String role) {

        return passengerService.getAllPassengers(username, role);
    }

    @PutMapping("/{id}")
    public Passenger updatePassenger(
            @PathVariable Long id,
            @Valid @RequestBody Passenger passenger,
            @RequestHeader("X-Username") String username,
            @RequestHeader(value = "X-Role", defaultValue = "ROLE_USER") String role) {

        return passengerService.updatePassenger(
                id,
                passenger,
                username,
                role
        );
    }

    @DeleteMapping("/{id}")
    public MessageResponse deletePassenger(
            @PathVariable Long id,
            @RequestHeader("X-Username") String username,
            @RequestHeader(value = "X-Role", defaultValue = "ROLE_USER") String role) {

        passengerService.deletePassenger(id, username, role);

        return new MessageResponse("Passenger deleted successfully");
    }
}