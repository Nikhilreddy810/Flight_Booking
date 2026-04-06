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

import org.springframework.beans.factory.annotation.Autowired;
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
        if (role.equals("ROLE_ADMIN")) {
            return bookingRepository.findAll();
        }
        return bookingRepository.findByCreatedBy(username);
    }

    @Transactional
    public Booking createBooking(BookingRequest request, String username) {
        Flight flight = flightRepository.findById(request.getFlightId())
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found"));

        Passenger passenger = passengerRepository.findById(request.getPassengerId())
                .orElseThrow(() -> new ResourceNotFoundException("Passenger not found"));

        if (flight.getTotalSeats() <= 0) {
            throw new NoSeatsAvailableException("No seats available for this flight");
        }

        flight.setTotalSeats(flight.getTotalSeats() - 1);
        flightRepository.save(flight);

        Booking booking = new Booking();
        booking.setFlight(flight);
        booking.setPassenger(passenger);
        booking.setBookingDate(LocalDate.now());
        booking.setCreatedBy(username);

        return bookingRepository.save(booking);
    }

    @Transactional
    public void cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        Flight flight = booking.getFlight();
        flight.setTotalSeats(flight.getTotalSeats() + 1);
        flightRepository.save(flight);

        bookingRepository.deleteById(id);
    }
}