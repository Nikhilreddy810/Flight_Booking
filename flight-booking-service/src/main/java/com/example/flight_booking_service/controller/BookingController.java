package com.example.flight_booking_service.controller;

import com.example.flight_booking_service.dto.BookingRequest;
import com.example.flight_booking_service.dto.MessageResponse;
import com.example.flight_booking_service.entity.Booking;
import com.example.flight_booking_service.service.BookingService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @GetMapping
    public List<Booking> getAllBookings(
            Authentication authentication) {

        String username = authentication.getName();
        String role = getRole(authentication);

        return bookingService.getAllBookings(
                username,
                role
        );
    }

    @PostMapping
    public Booking createBooking(
            @Valid @RequestBody BookingRequest request,
            Authentication authentication) {

        return bookingService.createBooking(
                request,
                authentication.getName()
        );
    }

    @DeleteMapping("/{id}")
    public MessageResponse cancelBooking(
            @PathVariable Long id,
            Authentication authentication) {

        String username = authentication.getName();
        String role = getRole(authentication);

        bookingService.cancelBooking(
                id,
                username,
                role
        );

        return new MessageResponse(
                "Booking cancelled successfully"
        );
    }

    private String getRole(Authentication authentication) {

        return authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER");
    }
}