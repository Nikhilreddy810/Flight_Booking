package com.example.flight.controller;

import com.example.flight.dto.BookingRequest;
import com.example.flight.dto.MessageResponse;
import com.example.flight.entity.Booking;
import com.example.flight.service.BookingService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@SecurityRequirement(name = "bearerAuth")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @GetMapping
    public List<Booking> getAllBookings(Authentication authentication) {
        return bookingService.getAllBookings(authentication.getName(), roleOf(authentication));
    }

    @PostMapping
    public Booking createBooking(@Valid @RequestBody BookingRequest request,
                                 Authentication authentication) {
        return bookingService.createBooking(request, authentication.getName());
    }

    @DeleteMapping("/{id}")
    public MessageResponse cancelBooking(@PathVariable Long id,
                                         Authentication authentication) {
        bookingService.cancelBooking(id, authentication.getName(), roleOf(authentication));
        return new MessageResponse("Booking cancelled successfully");
    }

    private String roleOf(Authentication authentication) {
        return authentication.getAuthorities().iterator().next().getAuthority();
    }
}
