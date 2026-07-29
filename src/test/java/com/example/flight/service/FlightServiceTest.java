package com.example.flight.service;

import com.example.flight.entity.Flight;
import com.example.flight.exception.ResourceNotFoundException;
import com.example.flight.repository.FlightRepository;

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
public class FlightServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @InjectMocks
    private FlightService flightService;

    private Flight stored;

    @BeforeEach
    void setUp() {
        stored = new Flight();
        stored.setId(1L);
        stored.setFlightNumber("AI101");
        stored.setAirline("Air India");
        stored.setSource("Hyderabad");
        stored.setDestination("Delhi");
        stored.setTotalSeats(100);
        stored.setAvailableSeats(100);
        stored.setPrice(4500.0);
    }

    @Test
    void shouldOpenAllSeats_whenFlightIsAdded() {
        Flight incoming = new Flight();
        incoming.setTotalSeats(100);
        when(flightRepository.save(any(Flight.class))).thenAnswer(i -> i.getArgument(0));

        Flight saved = flightService.addFlight(incoming);

        assertEquals(100, saved.getAvailableSeats());
    }

    @Test
    void shouldPreserveBookedSeats_whenFlightIsUpdated() {
        stored.setAvailableSeats(90); // 10 seats already sold
        Flight incoming = new Flight();
        incoming.setFlightNumber("AI102");
        incoming.setAirline("Air India");
        incoming.setSource("Hyderabad");
        incoming.setDestination("Mumbai");
        incoming.setTotalSeats(100);
        incoming.setPrice(5000.0);

        when(flightRepository.findById(1L)).thenReturn(Optional.of(stored));
        when(flightRepository.save(any(Flight.class))).thenAnswer(i -> i.getArgument(0));

        Flight result = flightService.updateFlight(1L, incoming);

        assertEquals("AI102", result.getFlightNumber());
        assertEquals(90, result.getAvailableSeats(), "the 10 sold seats must stay sold");
    }

    @Test
    void shouldShiftAvailability_whenCapacityShrinks() {
        stored.setAvailableSeats(90); // 10 sold
        Flight incoming = new Flight();
        incoming.setTotalSeats(50);

        when(flightRepository.findById(1L)).thenReturn(Optional.of(stored));
        when(flightRepository.save(any(Flight.class))).thenAnswer(i -> i.getArgument(0));

        Flight result = flightService.updateFlight(1L, incoming);

        assertEquals(40, result.getAvailableSeats());
    }

    @Test
    void shouldThrowNotFound_whenUpdatingMissingFlight() {
        when(flightRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            flightService.updateFlight(1L, new Flight());
        });
    }

    @Test
    void shouldThrowNotFound_whenDeletingMissingFlight() {
        when(flightRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            flightService.deleteFlight(1L);
        });
    }
}
