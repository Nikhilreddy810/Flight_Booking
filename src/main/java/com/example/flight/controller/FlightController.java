package com.example.flight.controller;
import com.example.flight.entity.Flight;
import com.example.flight.service.FlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import static org.springframework.http.ResponseEntity.*;

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
    public Flight addFlight(@RequestBody Flight flight) {
        return flightService.addFlight(flight);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Flight> getFlightById(@PathVariable Long id) {
        return flightService.getFlightById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Flight> updateFlight(@PathVariable Long id,
                                               @RequestBody Flight updatedFlight) {
        return flightService.getFlightById(id)
                .map(existing -> {
                    Flight saved = flightService.updateFlight(id, updatedFlight);
                    return ok(saved);
                })
                .orElseGet(() -> notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFlight(@PathVariable Long id) {
        return flightService.getFlightById(id)
                .map(flight -> {
                    flightService.deleteFlight(id);
                    return ResponseEntity.noContent().build();
                })
                .orElseGet(() -> notFound().build());
    }
}