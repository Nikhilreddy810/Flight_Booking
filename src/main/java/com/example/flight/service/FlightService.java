package com.example.flight.service;
import com.example.flight.entity.Flight;
import com.example.flight.exception.ResourceNotFoundException;
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
        flight.setId(null);
        // A new flight starts out fully available; callers only supply totalSeats.
        flight.setAvailableSeats(flight.getTotalSeats());
        return flightRepository.save(flight);
    }

    public Optional<Flight> getFlightById(Long id) {
        return flightRepository.findById(id);
    }

    public Flight getFlightByIdOrThrow(Long id) {
        return flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found with id: " + id));
    }

    @CacheEvict(value = "flights", allEntries = true)
    public void deleteFlight(Long id) {
        Flight flight = getFlightByIdOrThrow(id);
        flightRepository.delete(flight);
    }

    /**
     * Merges the incoming values onto the stored flight rather than overwriting the
     * row wholesale, so seats already booked are preserved. Resizing the aircraft
     * shifts availableSeats by the same amount.
     */
    @CacheEvict(value = "flights", allEntries = true)
    public Flight updateFlight(Long id, Flight updatedFlight) {
        Flight existing = getFlightByIdOrThrow(id);

        int bookedSeats = existing.getTotalSeats() - existing.getAvailableSeats();

        existing.setFlightNumber(updatedFlight.getFlightNumber());
        existing.setAirline(updatedFlight.getAirline());
        existing.setSource(updatedFlight.getSource());
        existing.setDestination(updatedFlight.getDestination());
        existing.setPrice(updatedFlight.getPrice());
        existing.setTotalSeats(updatedFlight.getTotalSeats());
        existing.setAvailableSeats(Math.max(0, updatedFlight.getTotalSeats() - bookedSeats));

        return flightRepository.save(existing);
    }
}
