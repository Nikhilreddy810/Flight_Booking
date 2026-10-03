package com.example.flight_flight_service.service;

import com.example.flight_flight_service.dto.FlightRequest;
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
    private FlightRequest flightRequest;

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

        flightRequest = new FlightRequest();

        flightRequest.setFlightNumber("FL1001");
        flightRequest.setAirline("IndiGo");
        flightRequest.setSource("Hyderabad");
        flightRequest.setDestination("Delhi");
        flightRequest.setTotalSeats(100);
        flightRequest.setPrice(5000.0);
    }

    @Test
    void getAllFlights_success() {

        when(flightRepository.findAll())
                .thenReturn(List.of(flight));

        List<Flight> result =
                flightService.getAllFlights();

        assertNotNull(result);
        assertEquals(1, result.size());

        assertEquals(
                "FL1001",
                result.get(0).getFlightNumber()
        );

        verify(flightRepository)
                .findAll();
    }

    @Test
    void addFlight_success() {

        when(flightRepository.save(any(Flight.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        Flight result =
                flightService.addFlight(flightRequest);

        assertNotNull(result);

        assertEquals(
                "FL1001",
                result.getFlightNumber()
        );

        assertEquals(
                "IndiGo",
                result.getAirline()
        );

        assertEquals(
                "Hyderabad",
                result.getSource()
        );

        assertEquals(
                "Delhi",
                result.getDestination()
        );

        assertEquals(
                100,
                result.getTotalSeats()
        );

        assertEquals(
                100,
                result.getAvailableSeats()
        );

        assertEquals(
                5000.0,
                result.getPrice()
        );

        verify(flightRepository)
                .save(any(Flight.class));
    }

    @Test
    void getFlightById_success() {

        when(flightRepository.findById(1L))
                .thenReturn(Optional.of(flight));

        Flight result =
                flightService.getFlightByIdOrThrow(1L);

        assertNotNull(result);

        assertEquals(
                1L,
                result.getId()
        );

        assertEquals(
                "FL1001",
                result.getFlightNumber()
        );

        verify(flightRepository)
                .findById(1L);
    }

    @Test
    void getFlightById_notFound() {

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
    void deleteFlight_notFound() {

        when(flightRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> flightService.deleteFlight(99L)
        );

        verify(flightRepository)
                .findById(99L);

        verify(
                flightRepository,
                never()
        ).delete(any(Flight.class));
    }

    @Test
    void updateFlight_success() {

        /*
         * Existing flight:
         *
         * Total seats = 100
         * Available seats = 90
         * Booked seats = 10
         */

        flight.setTotalSeats(100);
        flight.setAvailableSeats(90);

        FlightRequest updatedRequest =
                new FlightRequest();

        updatedRequest.setFlightNumber("FL2001");
        updatedRequest.setAirline("Air India");
        updatedRequest.setSource("Mumbai");
        updatedRequest.setDestination("Bangalore");
        updatedRequest.setTotalSeats(120);
        updatedRequest.setPrice(6000.0);

        when(flightRepository.findById(1L))
                .thenReturn(Optional.of(flight));

        when(flightRepository.save(any(Flight.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        Flight result =
                flightService.updateFlight(
                        1L,
                        updatedRequest
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
                120,
                result.getTotalSeats()
        );

        assertEquals(
                6000.0,
                result.getPrice()
        );

        /*
         * 120 total seats
         * 10 already booked
         * 110 available
         */
        assertEquals(
                110,
                result.getAvailableSeats()
        );

        verify(flightRepository)
                .save(flight);
    }

    @Test
    void updateFlight_notFound() {

        when(flightRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> flightService.updateFlight(
                        99L,
                        flightRequest
                )
        );

        verify(flightRepository)
                .findById(99L);

        verify(
                flightRepository,
                never()
        ).save(any(Flight.class));
    }

    @Test
    void updateFlight_totalSeatsLessThanBookedSeats() {

        /*
         * Total seats = 100
         * Available seats = 60
         * Booked seats = 40
         */

        flight.setTotalSeats(100);
        flight.setAvailableSeats(60);

        FlightRequest updatedRequest =
                new FlightRequest();

        updatedRequest.setFlightNumber("FL2001");
        updatedRequest.setAirline("Air India");
        updatedRequest.setSource("Mumbai");
        updatedRequest.setDestination("Delhi");
        updatedRequest.setTotalSeats(30);
        updatedRequest.setPrice(6000.0);

        when(flightRepository.findById(1L))
                .thenReturn(Optional.of(flight));

        assertThrows(
                IllegalArgumentException.class,
                () -> flightService.updateFlight(
                        1L,
                        updatedRequest
                )
        );

        verify(
                flightRepository,
                never()
        ).save(any(Flight.class));
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

        verify(
                flightRepository,
                never()
        ).save(any(Flight.class));
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

        verify(
                flightRepository,
                never()
        ).save(any(Flight.class));
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

        verify(
                flightRepository,
                never()
        ).save(any(Flight.class));
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

        verify(
                flightRepository,
                never()
        ).save(any(Flight.class));
    }
}