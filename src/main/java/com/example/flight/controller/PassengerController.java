package com.example.flight.controller;

import com.example.flight.dto.MessageResponse;
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
        return passengerService.savePassenger(passenger, authentication.getName());
    }

    @GetMapping
    public List<Passenger> getAllPassengers(Authentication authentication) {
        return passengerService.getAllPassengers(authentication.getName(), roleOf(authentication));
    }

    @PutMapping("/{id}")
    public Passenger updatePassenger(@PathVariable Long id,
                                     @Valid @RequestBody Passenger passenger,
                                     Authentication authentication) {
        return passengerService.updatePassenger(id, passenger,
                authentication.getName(), roleOf(authentication));
    }

    @DeleteMapping("/{id}")
    public MessageResponse deletePassenger(@PathVariable Long id,
                                           Authentication authentication) {
        passengerService.deletePassenger(id, authentication.getName(), roleOf(authentication));
        return new MessageResponse("Passenger deleted successfully");
    }

    private String roleOf(Authentication authentication) {
        return authentication.getAuthorities().iterator().next().getAuthority();
    }
}
