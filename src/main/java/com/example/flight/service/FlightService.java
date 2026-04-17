package com.example.flight.service;
import com.example.flight.entity.Flight;
import com.example.flight.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class FlightService {

    @Autowired
    private FlightRepository flightRepository;

    @Cacheable("flights")
    public List<Flight> getAllFlights() {
        return flightRepository.findAll();
    }

    @CacheEvict(value = "flights", allEntries = true)
    public Flight addFlight(Flight flight) {
        return flightRepository.save(flight);
    }

    public Optional<Flight> getFlightById(Long id) {
        return flightRepository.findById(id);
    }

    @CacheEvict(value = "flights", allEntries = true)
    public void deleteFlight(Long id) {
        flightRepository.deleteById(id);
    }

    @CacheEvict(value = "flights", allEntries = true)
    public Flight updateFlight(Long id, Flight updatedFlight) {
        updatedFlight.setId(id);
        return flightRepository.save(updatedFlight);
    }
}