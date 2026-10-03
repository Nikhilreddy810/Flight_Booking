package com.example.flight_flight_service.service;

import com.example.flight_flight_service.dto.FlightRequest;
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

@Service
public class FlightService {

    @Autowired
    private FlightRepository flightRepository;

    @Cacheable("flights")
    public List<Flight> getAllFlights() {

        return flightRepository.findAll();
    }

    @CacheEvict(value = "flights", allEntries = true)
    public Flight addFlight(FlightRequest request) {

        Flight flight = new Flight();

        flight.setFlightNumber(request.getFlightNumber());
        flight.setAirline(request.getAirline());
        flight.setSource(request.getSource());
        flight.setDestination(request.getDestination());
        flight.setTotalSeats(request.getTotalSeats());
        flight.setPrice(request.getPrice());

        // Server controls available seats.
        flight.setAvailableSeats(
                request.getTotalSeats()
        );

        return flightRepository.save(flight);
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

        Flight flight =
                getFlightByIdOrThrow(id);

        flightRepository.delete(flight);
    }

    @CacheEvict(value = "flights", allEntries = true)
    public Flight updateFlight(
            Long id,
            FlightRequest request) {

        Flight existing =
                getFlightByIdOrThrow(id);

        int bookedSeats =
                existing.getTotalSeats()
                        - existing.getAvailableSeats();

        if (request.getTotalSeats() < bookedSeats) {

            throw new IllegalArgumentException(
                    "Total seats cannot be less than already booked seats: "
                            + bookedSeats
            );
        }

        existing.setFlightNumber(
                request.getFlightNumber()
        );

        existing.setAirline(
                request.getAirline()
        );

        existing.setSource(
                request.getSource()
        );

        existing.setDestination(
                request.getDestination()
        );

        existing.setPrice(
                request.getPrice()
        );

        existing.setTotalSeats(
                request.getTotalSeats()
        );

        existing.setAvailableSeats(
                request.getTotalSeats()
                        - bookedSeats
        );

        return flightRepository.save(existing);
    }

    @Transactional
    @CacheEvict(value = "flights", allEntries = true)
    public Flight reserveSeat(Long flightId) {

        Flight flight =
                flightRepository
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

    @Transactional
    @CacheEvict(value = "flights", allEntries = true)
    public Flight releaseSeat(Long flightId) {

        Flight flight =
                flightRepository
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