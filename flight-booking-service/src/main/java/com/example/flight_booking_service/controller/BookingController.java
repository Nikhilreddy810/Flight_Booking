package com.example.flight_booking_service.controller;

import com.example.flight_booking_service.dto.BookingRequest;
import com.example.flight_booking_service.dto.MessageResponse;
import com.example.flight_booking_service.entity.Booking;
import com.example.flight_booking_service.service.BookingService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @GetMapping
    public List<Booking> getAllBookings(
            @RequestHeader("X-Username") String username,
            @RequestHeader(value = "X-Role", defaultValue = "ROLE_USER") String role) {

        return bookingService.getAllBookings(username, role);
    }

    @PostMapping
    public Booking createBooking(
            @Valid @RequestBody BookingRequest request,
            @RequestHeader("X-Username") String username) {

        return bookingService.createBooking(request, username);
    }

    @DeleteMapping("/{id}")
    public MessageResponse cancelBooking(
            @PathVariable Long id,
            @RequestHeader("X-Username") String username,
            @RequestHeader(value = "X-Role", defaultValue = "ROLE_USER") String role) {

        bookingService.cancelBooking(id, username, role);

        return new MessageResponse("Booking cancelled successfully");
    }
}