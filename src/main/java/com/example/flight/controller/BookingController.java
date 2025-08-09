package com.example.flight.controller;

import com.example.flight.entity.Booking;
import com.example.flight.repository.BookingRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.ResponseEntity.*; // ✅ required for noContent() and notFound()

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingRepository bookingRepository;

    @GetMapping
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @PostMapping
    public Booking createBooking(@RequestBody Booking booking) {
        return bookingRepository.save(booking);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(@PathVariable Long id) {
        return bookingRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Booking> updateBooking(@PathVariable Long id, @RequestBody Booking updatedBooking) {
        return bookingRepository.findById(id)
                .map(existingBooking -> {
                    updatedBooking.setId(id); // ✅ use `setId` not `setBookingId`
                    Booking savedBooking = bookingRepository.save(updatedBooking);
                    return ok(savedBooking);
                })
                .orElseGet(() -> notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteBooking(@PathVariable Long id) {
        return bookingRepository.findById(id)
                .map(booking -> {
                    bookingRepository.deleteById(id);
                    return noContent().build(); // ✅ works now due to static import
                })
                .orElseGet(() -> notFound().build());
    }
}
