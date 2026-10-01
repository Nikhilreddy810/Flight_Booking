package com.example.flight_booking_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.flight_booking_service.entity.Passenger;
import java.util.List;

public interface PassengerRepository extends JpaRepository<Passenger, Long> {
    List<Passenger> findByCreatedBy(String createdBy);
}