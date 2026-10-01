package com.example.flight_flight_service.service;

import com.example.flight_flight_service.entity.Flight;
import com.example.flight_flight_service.exception.NoSeatsAvailableException;
import com.example.flight_flight_service.exception.ResourceNotFoundException;
import com.example.flight_flight_service.repository.FlightRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlightServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @InjectMocks
    private FlightService flightService;

    private Flight flight;

    @BeforeEach
    void setUp() {

        flight = new Flight();

        flight.setId(1L);
        flight.setFlightNumber("FL1001");
        flight.setAirline("IndiGo");
        flight.setSource("Hyderabad");
        flight.setDestination("Delhi");
        flight.setTotalSeats(100);
        flight.setAvailableSeats(100);
        flight.setPrice(5000.0);
    }

    @Test
    void getAllFlights_success() {

        when(flightRepository.findAll())
                .thenReturn(List.of(flight));

        List<Flight> result =
                flightService.getAllFlights();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("FL1001",
                result.get(0).getFlightNumber());

        verify(flightRepository)
                .findAll();
    }

    @Test
    void addFlight_success() {

        flight.setId(99L);
        flight.setAvailableSeats(20);

        when(flightRepository.save(any(Flight.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        Flight result =
                flightService.addFlight(flight);

        assertNull(result.getId());
        assertEquals(
                result.getTotalSeats(),
                result.getAvailableSeats()
        );

        assertEquals(100,
                result.getAvailableSeats());

        verify(flightRepository)
                .save(flight);
    }

    @Test
    void getFlightById_success() {

        when(flightRepository.findById(1L))
                .thenReturn(Optional.of(flight));

        Optional<Flight> result =
                flightService.getFlightById(1L);

        assertTrue(result.isPresent());
        assertEquals(
                "FL1001",
                result.get().getFlightNumber()
        );

        verify(flightRepository)
                .findById(1L);
    }

    @Test
    void getFlightByIdOrThrow_success() {

        when(flightRepository.findById(1L))
                .thenReturn(Optional.of(flight));

        Flight result =
                flightService.getFlightByIdOrThrow(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());

        verify(flightRepository)
                .findById(1L);
    }

    @Test
    void getFlightByIdOrThrow_notFound() {

        when(flightRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> flightService.getFlightByIdOrThrow(99L)
        );

        verify(flightRepository)
                .findById(99L);
    }

    @Test
    void deleteFlight_success() {

        when(flightRepository.findById(1L))
                .thenReturn(Optional.of(flight));

        flightService.deleteFlight(1L);

        verify(flightRepository)
                .findById(1L);

        verify(flightRepository)
                .delete(flight);
    }

    @Test
    void updateFlight_success() {

        Flight updatedFlight = new Flight();

        updatedFlight.setFlightNumber("FL2001");
        updatedFlight.setAirline("Air India");
        updatedFlight.setSource("Mumbai");
        updatedFlight.setDestination("Bangalore");
        updatedFlight.setPrice(6000.0);
        updatedFlight.setTotalSeats(120);

        flight.setTotalSeats(100);
        flight.setAvailableSeats(90);

        when(flightRepository.findById(1L))
                .thenReturn(Optional.of(flight));

        when(flightRepository.save(any(Flight.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        Flight result =
                flightService.updateFlight(
                        1L,
                        updatedFlight
                );

        assertEquals(
                "FL2001",
                result.getFlightNumber()
        );

        assertEquals(
                "Air India",
                result.getAirline()
        );

        assertEquals(
                "Mumbai",
                result.getSource()
        );

        assertEquals(
                "Bangalore",
                result.getDestination()
        );

        assertEquals(
                6000.0,
                result.getPrice()
        );

        assertEquals(
                120,
                result.getTotalSeats()
        );

        /*
         * Originally:
         * totalSeats = 100
         * availableSeats = 90
         *
         * bookedSeats = 10
         *
         * New totalSeats = 120
         *
         * New availableSeats = 120 - 10 = 110
         */
        assertEquals(
                110,
                result.getAvailableSeats()
        );

        verify(flightRepository)
                .save(flight);
    }

    @Test
    void reserveSeat_success() {

        flight.setAvailableSeats(100);

        when(flightRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(flight));

        when(flightRepository.save(any(Flight.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        Flight result =
                flightService.reserveSeat(1L);

        assertEquals(
                99,
                result.getAvailableSeats()
        );

        verify(flightRepository)
                .findByIdForUpdate(1L);

        verify(flightRepository)
                .save(flight);
    }

    @Test
    void reserveSeat_noSeatsAvailable() {

        flight.setAvailableSeats(0);

        when(flightRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(flight));

        assertThrows(
                NoSeatsAvailableException.class,
                () -> flightService.reserveSeat(1L)
        );

        verify(flightRepository)
                .findByIdForUpdate(1L);

        verify(flightRepository, never())
                .save(any(Flight.class));
    }

    @Test
    void reserveSeat_flightNotFound() {

        when(flightRepository.findByIdForUpdate(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> flightService.reserveSeat(99L)
        );

        verify(flightRepository)
                .findByIdForUpdate(99L);

        verify(flightRepository, never())
                .save(any(Flight.class));
    }

    @Test
    void releaseSeat_success() {

        flight.setTotalSeats(100);
        flight.setAvailableSeats(99);

        when(flightRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(flight));

        when(flightRepository.save(any(Flight.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        Flight result =
                flightService.releaseSeat(1L);

        assertEquals(
                100,
                result.getAvailableSeats()
        );

        verify(flightRepository)
                .findByIdForUpdate(1L);

        verify(flightRepository)
                .save(flight);
    }

    @Test
    void releaseSeat_flightNotFound() {

        when(flightRepository.findByIdForUpdate(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> flightService.releaseSeat(99L)
        );

        verify(flightRepository)
                .findByIdForUpdate(99L);

        verify(flightRepository, never())
                .save(any(Flight.class));
    }

    @Test
    void releaseSeat_whenAlreadyFull_doesNotIncreaseSeats() {

        flight.setTotalSeats(100);
        flight.setAvailableSeats(100);

        when(flightRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(flight));

        Flight result =
                flightService.releaseSeat(1L);

        assertEquals(
                100,
                result.getAvailableSeats()
        );

        verify(flightRepository)
                .findByIdForUpdate(1L);

        verify(flightRepository, never())
                .save(any(Flight.class));
    }
}