package com.example.flight.service;

import com.example.flight.entity.Flight;
import com.example.flight.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FlightService {

    @Autowired
    private FlightRepository flightRepository;

    public List<Flight> getAllFlights() {
        return flightRepository.findAll();
    }

    public Flight addFlight(Flight flight) {
        return flightRepository.save(flight);
    }

    public Optional<Flight> getFlightById(Long id) {
        return flightRepository.findById(id);
    }

    public void deleteFlight(Long id) {
        flightRepository.deleteById(id);
    }

    public Flight updateFlight(Long id, Flight updatedFlight) {
        updatedFlight.setId(id);
        return flightRepository.save(updatedFlight);
    }
}
