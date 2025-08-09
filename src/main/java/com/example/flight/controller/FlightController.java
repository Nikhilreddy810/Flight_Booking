package com.example.flight.controller;

import com.example.flight.entity.Flight;
import com.example.flight.repository.FlightRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.ResponseEntity.*;

@RestController
@RequestMapping("/api/flights")
public class FlightController {

    @Autowired
    private FlightRepository flightRepository;

    @GetMapping
    public List<Flight> getAllFlights() {
        return flightRepository.findAll();
    }

    @PostMapping
    public Flight addFlight(@RequestBody Flight flight) {
        return flightRepository.save(flight);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Flight> getFlightById(@PathVariable Long id) {
        return flightRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Flight> updateFlight(@PathVariable Long id, @RequestBody Flight updatedFlight) {
        return flightRepository.findById(id)
                .map(existing -> {
                    updatedFlight.setId(id);
                    Flight saved = flightRepository.save(updatedFlight);
                    return ok(saved);
                })
                .orElseGet(() -> notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteFlight(@PathVariable Long id) {
        return flightRepository.findById(id)
                .map(flight -> {
                    flightRepository.deleteById(id);
                    return noContent().build();
                })
                .orElseGet(() -> notFound().build());
    }
}
