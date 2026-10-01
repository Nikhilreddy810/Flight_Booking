package com.example.flight_booking_service.service;

import com.example.flight_booking_service.dto.BookingRequest;
import com.example.flight_booking_service.entity.Booking;
import com.example.flight_booking_service.entity.Passenger;
import com.example.flight_booking_service.exception.AccessDeniedException;
import com.example.flight_booking_service.exception.NoSeatsAvailableException;
import com.example.flight_booking_service.exception.ResourceNotFoundException;
import com.example.flight_booking_service.repository.BookingRepository;
import com.example.flight_booking_service.repository.PassengerRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PassengerRepository passengerRepository;

    @Autowired
    private RestClient restClient;

    public List<Booking> getAllBookings(
            String username,
            String role) {

        if ("ROLE_ADMIN".equals(role)) {
            return bookingRepository.findAll();
        }

        return bookingRepository.findByCreatedBy(username);
    }

    public Booking createBooking(
            BookingRequest request,
            String username) {

        Passenger passenger = passengerRepository
                .findById(request.getPassengerId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Passenger not found with id: "
                                        + request.getPassengerId()
                        )
                );

        reserveFlightSeat(request.getFlightId());

        Booking booking = new Booking();

        booking.setFlightId(request.getFlightId());
        booking.setPassenger(passenger);
        booking.setBookingDate(LocalDate.now());
        booking.setCreatedBy(username);

        try {

            return bookingRepository.save(booking);

        } catch (RuntimeException ex) {

            try {
                releaseFlightSeat(request.getFlightId());
            } catch (Exception ignored) {
            }

            throw ex;
        }
    }

    public void cancelBooking(
            Long id,
            String username,
            String role) {

        Booking booking = bookingRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Booking not found with id: " + id
                        )
                );

        if (!"ROLE_ADMIN".equals(role)
                && !username.equals(booking.getCreatedBy())) {

            throw new AccessDeniedException(
                    "You are not allowed to cancel this booking"
            );
        }

        releaseFlightSeat(booking.getFlightId());

        bookingRepository.delete(booking);
    }

    private void reserveFlightSeat(Long flightId) {

        try {

            restClient.post()
                    .uri(
                            "/api/flights/{id}/reserve-seat",
                            flightId
                    )
                    .retrieve()
                    .toBodilessEntity();

        } catch (HttpClientErrorException.BadRequest ex) {

            throw new NoSeatsAvailableException(
                    "No seats available for this flight"
            );

        } catch (HttpClientErrorException.NotFound ex) {

            throw new ResourceNotFoundException(
                    "Flight not found with id: " + flightId
            );
        }
    }

    private void releaseFlightSeat(Long flightId) {

        try {

            restClient.post()
                    .uri(
                            "/api/flights/{id}/release-seat",
                            flightId
                    )
                    .retrieve()
                    .toBodilessEntity();

        } catch (HttpClientErrorException.NotFound ex) {

            throw new ResourceNotFoundException(
                    "Flight not found with id: " + flightId
            );
        }
    }
}