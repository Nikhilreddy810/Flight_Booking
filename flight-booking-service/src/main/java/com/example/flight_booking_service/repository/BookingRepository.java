package com.example.flight_booking_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.flight_booking_service.entity.Booking;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByCreatedBy(String createdBy);
}