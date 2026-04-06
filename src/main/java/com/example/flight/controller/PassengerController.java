package com.example.flight.controller;

import com.example.flight.entity.Passenger;
import com.example.flight.service.PassengerService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/passengers")
@SecurityRequirement(name = "bearerAuth")
public class PassengerController {

    @Autowired
    private PassengerService passengerService;

    @PostMapping
    public Passenger createPassenger(@Valid @RequestBody Passenger passenger,
                                     Authentication authentication) {
        String username = authentication.getName();
        return passengerService.savePassenger(passenger, username);
    }

    @GetMapping
    public List<Passenger> getAllPassengers(Authentication authentication) {
        String username = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        return passengerService.getAllPassengers(username, role);
    }

    @PutMapping("/{id}")
    public Passenger updatePassenger(@PathVariable Long id,
                                     @Valid @RequestBody Passenger passenger) {
        return passengerService.updatePassenger(id, passenger);
    }

    @DeleteMapping("/{id}")
    public String deletePassenger(@PathVariable Long id) {
        passengerService.deletePassenger(id);
        return "Passenger deleted successfully";
    }
}