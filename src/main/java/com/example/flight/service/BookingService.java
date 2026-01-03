package com.example.flight.service;

import com.example.flight.entity.Booking;
import com.example.flight.entity.Flight;
import com.example.flight.exception.ResourceNotFoundException;
import com.example.flight.exception.NoSeatsAvailableException;
import com.example.flight.repository.BookingRepository;
import com.example.flight.repository.FlightRepository;
import com.example.flight.repository.PassengerRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private PassengerRepository passengerRepository;

    // CREATE BOOKING
    public Booking createBooking(Booking booking) {

        Flight flight = flightRepository.findById(booking.getFlightId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Flight not found"));

        passengerRepository.findById(booking.getPassengerId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Passenger not found"));

        if (flight.getTotalSeats() <= 0) {
            throw new NoSeatsAvailableException("No seats available for this flight");
        }

        flight.setTotalSeats(flight.getTotalSeats() - 1);
        flightRepository.save(flight);

        return bookingRepository.save(booking);
    }

    // READ ALL BOOKINGS
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    // CANCEL BOOKING (DELETE + RESTORE SEAT)
    public void cancelBooking(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Booking not found"));

        Flight flight = flightRepository.findById(booking.getFlightId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Flight not found"));

        // Restore seat
        flight.setTotalSeats(flight.getTotalSeats() + 1);
        flightRepository.save(flight);

        // Delete booking
        bookingRepository.delete(booking);
    }
}
