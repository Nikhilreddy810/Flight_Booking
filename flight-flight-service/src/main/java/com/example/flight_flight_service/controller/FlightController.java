package com.example.flight_flight_service.controller;

import com.example.flight_flight_service.dto.FlightRequest;
import com.example.flight_flight_service.entity.Flight;
import com.example.flight_flight_service.service.FlightService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flights")
public class FlightController {

    @Autowired
    private FlightService flightService;

    @GetMapping
    public List<Flight> getAllFlights() {

        return flightService.getAllFlights();
    }

    @PostMapping
    public Flight addFlight(
            @Valid @RequestBody FlightRequest request) {

        return flightService.addFlight(request);
    }

    @GetMapping("/{id}")
    public Flight getFlightById(
            @PathVariable("id") Long id) {

        return flightService.getFlightByIdOrThrow(id);
    }

    @PutMapping("/{id}")
    public Flight updateFlight(
            @PathVariable("id") Long id,
            @Valid @RequestBody FlightRequest request) {

        return flightService.updateFlight(
                id,
                request
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFlight(
            @PathVariable("id") Long id) {

        flightService.deleteFlight(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reserve-seat")
    public Flight reserveSeat(
            @PathVariable("id") Long id) {

        return flightService.reserveSeat(id);
    }

    @PostMapping("/{id}/release-seat")
    public Flight releaseSeat(
            @PathVariable("id") Long id) {

        return flightService.releaseSeat(id);
    }
}