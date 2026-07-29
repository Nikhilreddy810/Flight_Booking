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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
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
        flight.setAvailableSeats(10);

        passenger = new Passenger();
        passenger.setId(1L);
        passenger.setName("Nikhil");

        request = new BookingRequest();
        request.setFlightId(1L);
        request.setPassengerId(1L);
    }

    private Booking bookingOwnedBy(String owner) {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setFlight(flight);
        booking.setPassenger(passenger);
        booking.setCreatedBy(owner);
        return booking;
    }

    @Test
    void shouldCreateBooking_whenSeatsAvailable() {
        when(flightRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(flight));
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

        Booking booking = bookingService.createBooking(request, "testuser");

        assertNotNull(booking);
        assertEquals(flight, booking.getFlight());
        assertEquals(passenger, booking.getPassenger());
        assertEquals("testuser", booking.getCreatedBy());
        assertEquals(9, flight.getAvailableSeats());
        assertEquals(10, flight.getTotalSeats(), "capacity must not change when a seat is sold");
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void shouldThrowException_whenNoSeatsAvailable() {
        flight.setAvailableSeats(0);
        when(flightRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(flight));
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(passenger));

        assertThrows(NoSeatsAvailableException.class, () -> {
            bookingService.createBooking(request, "testuser");
        });

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void shouldThrowException_whenFlightNotFound() {
        when(flightRepository.findByIdForUpdate(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            bookingService.createBooking(request, "testuser");
        });
    }

    @Test
    void shouldThrowException_whenPassengerNotFound() {
        when(flightRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(flight));
        when(passengerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            bookingService.createBooking(request, "testuser");
        });
    }

    @Test
    void shouldCancelBooking_andRestoreSeats() {
        Booking booking = bookingOwnedBy("testuser");
        flight.setAvailableSeats(9);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(flightRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(flight));

        bookingService.cancelBooking(1L, "testuser", Roles.USER);

        assertEquals(10, flight.getAvailableSeats());
        verify(bookingRepository, times(1)).delete(booking);
    }

    @Test
    void shouldNotRestoreSeatsBeyondCapacity() {
        Booking booking = bookingOwnedBy("testuser");

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(flightRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(flight));

        bookingService.cancelBooking(1L, "testuser", Roles.USER);

        assertEquals(10, flight.getAvailableSeats());
    }

    @Test
    void shouldThrowException_whenCancelBookingNotFound() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            bookingService.cancelBooking(1L, "testuser", Roles.USER);
        });
    }

    @Test
    void shouldDenyCancel_whenBookingBelongsToAnotherUser() {
        Booking booking = bookingOwnedBy("owner");
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(AccessDeniedException.class, () -> {
            bookingService.cancelBooking(1L, "intruder", Roles.USER);
        });

        verify(bookingRepository, never()).delete(any(Booking.class));
        assertEquals(10, flight.getAvailableSeats(), "a denied cancel must not release a seat");
    }

    @Test
    void shouldAllowAdminToCancelAnyBooking() {
        Booking booking = bookingOwnedBy("someoneelse");
        flight.setAvailableSeats(9);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(flightRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(flight));

        bookingService.cancelBooking(1L, "admin", Roles.ADMIN);

        assertEquals(10, flight.getAvailableSeats());
        verify(bookingRepository, times(1)).delete(booking);
    }

    @Test
    void shouldReturnAllBookings_forAdmin() {
        when(bookingRepository.findAll()).thenReturn(List.of(bookingOwnedBy("a")));

        bookingService.getAllBookings("admin", Roles.ADMIN);

        verify(bookingRepository).findAll();
        verify(bookingRepository, never()).findByCreatedBy(anyString());
    }

    @Test
    void shouldReturnOnlyOwnBookings_forUser() {
        when(bookingRepository.findByCreatedBy("testuser"))
                .thenReturn(List.of(bookingOwnedBy("testuser")));

        bookingService.getAllBookings("testuser", Roles.USER);

        verify(bookingRepository).findByCreatedBy("testuser");
        verify(bookingRepository, never()).findAll();
    }
}
