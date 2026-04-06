package com.example.flight.repository;

import com.example.flight.entity.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PassengerRepository extends JpaRepository<Passenger, Long> {
    List<Passenger> findByCreatedBy(String createdBy);
}