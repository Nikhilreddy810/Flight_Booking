package com.example.flight.service;

import com.example.flight.dto.BookingRequest;
import com.example.flight.entity.Booking;
import com.example.flight.entity.Flight;
import com.example.flight.entity.Passenger;
import com.example.flight.exception.NoSeatsAvailableException;
import com.example.flight.exception.ResourceNotFoundException;
import com.example.flight.repository.BookingRepository;
import com.example.flight.repository.FlightRepository;
import com.example.flight.repository.PassengerRepository;
import com.example.flight.security.Roles;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private PassengerRepository passengerRepository;

    public List<Booking> getAllBookings(String username, String role) {
        if (Roles.isAdmin(role)) {
            return bookingRepository.findAll();
        }
        return bookingRepository.findByCreatedBy(username);
    }

    @Transactional
    @CacheEvict(value = "flights", allEntries = true)
    public Booking createBooking(BookingRequest request, String username) {
        // Locked read: two concurrent bookings for the last seat must not both succeed.
        Flight flight = flightRepository.findByIdForUpdate(request.getFlightId())
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found"));

        Passenger passenger = passengerRepository.findById(request.getPassengerId())
                .orElseThrow(() -> new ResourceNotFoundException("Passenger not found"));

        if (flight.getAvailableSeats() <= 0) {
            throw new NoSeatsAvailableException("No seats available for this flight");
        }

        flight.setAvailableSeats(flight.getAvailableSeats() - 1);
        flightRepository.save(flight);

        Booking booking = new Booking();
        booking.setFlight(flight);
        booking.setPassenger(passenger);
        booking.setBookingDate(LocalDate.now());
        booking.setCreatedBy(username);

        return bookingRepository.save(booking);
    }

    @Transactional
    @CacheEvict(value = "flights", allEntries = true)
    public void cancelBooking(Long id, String username, String role) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        if (!Roles.isAdmin(role) && !username.equals(booking.getCreatedBy())) {
            throw new AccessDeniedException("You are not allowed to cancel this booking");
        }

        Flight flight = flightRepository.findByIdForUpdate(booking.getFlight().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found"));

        // Never restore past the aircraft's capacity.
        flight.setAvailableSeats(Math.min(flight.getTotalSeats(), flight.getAvailableSeats() + 1));
        flightRepository.save(flight);

        bookingRepository.delete(booking);
    }
}
