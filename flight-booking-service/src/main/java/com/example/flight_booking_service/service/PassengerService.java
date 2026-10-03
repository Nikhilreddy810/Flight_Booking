package com.example.flight_booking_service.service;

import com.example.flight_booking_service.dto.PassengerRequest;
import com.example.flight_booking_service.entity.Passenger;
import com.example.flight_booking_service.exception.AccessDeniedException;
import com.example.flight_booking_service.exception.ResourceNotFoundException;
import com.example.flight_booking_service.repository.PassengerRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PassengerService {

    @Autowired
    private PassengerRepository passengerRepository;

    public Passenger createPassenger(
            PassengerRequest request,
            String username) {

        Passenger passenger = new Passenger();

        passenger.setName(request.getName());
        passenger.setEmail(request.getEmail());
        passenger.setAge(request.getAge());
        passenger.setContact(request.getContact());

        // Never accept createdBy from the client.
        // It always comes from the authenticated JWT.
        passenger.setCreatedBy(username);

        return passengerRepository.save(passenger);
    }

    public List<Passenger> getAllPassengers(
            String username,
            String role) {

        if ("ROLE_ADMIN".equals(role)) {
            return passengerRepository.findAll();
        }

        return passengerRepository.findByCreatedBy(username);
    }

    public Passenger updatePassenger(
            Long id,
            PassengerRequest request,
            String username,
            String role) {

        Passenger existing =
                findOwned(id, username, role);

        existing.setName(request.getName());
        existing.setEmail(request.getEmail());
        existing.setAge(request.getAge());
        existing.setContact(request.getContact());

        // ID and createdBy remain unchanged.

        return passengerRepository.save(existing);
    }

    public void deletePassenger(
            Long id,
            String username,
            String role) {

        passengerRepository.delete(
                findOwned(id, username, role)
        );
    }

    private Passenger findOwned(
            Long id,
            String username,
            String role) {

        Passenger passenger =
                passengerRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Passenger not found with id: "
                                                + id
                                )
                        );

        if (!"ROLE_ADMIN".equals(role)
                && !username.equals(
                        passenger.getCreatedBy())) {

            throw new AccessDeniedException(
                    "You are not allowed to access this passenger"
            );
        }

        return passenger;
    }
}