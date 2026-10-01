package com.example.flight_flight_service.service;

import com.example.flight_flight_service.entity.Flight;
import com.example.flight_flight_service.exception.NoSeatsAvailableException;
import com.example.flight_flight_service.exception.ResourceNotFoundException;
import com.example.flight_flight_service.repository.FlightRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        flight.setAvailableSeats(
                flight.getTotalSeats()
        );

        return flightRepository.save(flight);
    }

    public Optional<Flight> getFlightById(Long id) {
        return flightRepository.findById(id);
    }

    public Flight getFlightByIdOrThrow(Long id) {

        return flightRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Flight not found with id: " + id
                        )
                );
    }

    @CacheEvict(value = "flights", allEntries = true)
    public void deleteFlight(Long id) {

        Flight flight = getFlightByIdOrThrow(id);

        flightRepository.delete(flight);
    }

    @CacheEvict(value = "flights", allEntries = true)
    public Flight updateFlight(
            Long id,
            Flight updatedFlight) {

        Flight existing = getFlightByIdOrThrow(id);

        int bookedSeats =
                existing.getTotalSeats()
                        - existing.getAvailableSeats();

        existing.setFlightNumber(
                updatedFlight.getFlightNumber()
        );

        existing.setAirline(
                updatedFlight.getAirline()
        );

        existing.setSource(
                updatedFlight.getSource()
        );

        existing.setDestination(
                updatedFlight.getDestination()
        );

        existing.setPrice(
                updatedFlight.getPrice()
        );

        existing.setTotalSeats(
                updatedFlight.getTotalSeats()
        );

        existing.setAvailableSeats(
                Math.max(
                        0,
                        updatedFlight.getTotalSeats()
                                - bookedSeats
                )
        );

        return flightRepository.save(existing);
    }

    /**
     * Reserve exactly one seat.
     *
     * The pessimistic database lock guarantees that
     * concurrent booking requests cannot reserve the
     * same available seat.
     */
    @Transactional
    @CacheEvict(value = "flights", allEntries = true)
    public Flight reserveSeat(Long flightId) {

        Flight flight = flightRepository
                .findByIdForUpdate(flightId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Flight not found with id: "
                                        + flightId
                        )
                );

        if (flight.getAvailableSeats() <= 0) {

            throw new NoSeatsAvailableException(
                    "No seats available for flight: "
                            + flightId
            );
        }

        flight.setAvailableSeats(
                flight.getAvailableSeats() - 1
        );

        return flightRepository.save(flight);
    }

    /**
     * Release exactly one seat.
     *
     * The same row lock is used so that concurrent
     * reserve/release operations remain consistent.
     */
    @Transactional
    @CacheEvict(value = "flights", allEntries = true)
    public Flight releaseSeat(Long flightId) {

        Flight flight = flightRepository
                .findByIdForUpdate(flightId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Flight not found with id: "
                                        + flightId
                        )
                );

        if (flight.getAvailableSeats()
                >= flight.getTotalSeats()) {

            return flight;
        }

        flight.setAvailableSeats(
                flight.getAvailableSeats() + 1
        );

        return flightRepository.save(flight);
    }
}