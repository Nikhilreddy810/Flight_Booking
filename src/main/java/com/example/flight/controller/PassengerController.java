package com.example.flight.controller;

import com.example.flight.entity.Passenger;
import com.example.flight.service.PassengerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/passengers")
public class PassengerController {

    @Autowired
    private PassengerService passengerService;

    @PostMapping
    public Passenger createPassenger(@RequestBody Passenger passenger) {
        return passengerService.savePassenger(passenger);
    }

    @GetMapping
    public List<Passenger> getAllPassengers() {
        return passengerService.getAllPassengers();
    }

    @PutMapping("/{id}")
    public Passenger updatePassenger(
            @PathVariable Long id,
            @RequestBody Passenger passenger) {
        return passengerService.updatePassenger(id, passenger);
    }
}
