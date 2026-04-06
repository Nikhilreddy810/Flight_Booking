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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private PassengerRepository passengerRepository;

    @InjectMocks
    private BookingService bookingService;

    private Flight flight;
    private Passenger passenger;
    private BookingRequest request;

    @BeforeEach
    void setUp() {
        flight = new Flight();
        flight.setId(1L);
        flight.setFlightNumber("AI101");
        flight.setTotalSeats(10);

        passenger = new Passenger();
        passenger.setId(1L);
        passenger.setName("Nikhil");

        request = new BookingRequest();
        request.setFlightId(1L);
        request.setPassengerId(1L);
    }

    @Test
    void shouldCreateBooking_whenSeatsAvailable() {
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

        Booking booking = bookingService.createBooking(request, "testuser");

        assertNotNull(booking);
        assertEquals(flight, booking.getFlight());
        assertEquals(passenger, booking.getPassenger());
        assertEquals("testuser", booking.getCreatedBy());
        assertEquals(9, flight.getTotalSeats());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void shouldThrowException_whenNoSeatsAvailable() {
        flight.setTotalSeats(0);
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));

        assertThrows(NoSeatsAvailableException.class, () -> {
            bookingService.createBooking(request, "testuser");
        });

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void shouldThrowException_whenFlightNotFound() {
        when(flightRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            bookingService.createBooking(request, "testuser");
        });
    }

    @Test
    void shouldThrowException_whenPassengerNotFound() {
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(passengerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            bookingService.createBooking(request, "testuser");
        });
    }

    @Test
    void shouldCancelBooking_andRestoreSeats() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setFlight(flight);
        booking.setPassenger(passenger);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        bookingService.cancelBooking(1L);

        assertEquals(11, flight.getTotalSeats());
        verify(bookingRepository, times(1)).deleteById(1L);
    }

    @Test
    void shouldThrowException_whenCancelBookingNotFound() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            bookingService.cancelBooking(1L);
        });
    }
}