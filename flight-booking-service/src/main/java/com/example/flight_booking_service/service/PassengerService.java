package com.example.flight_booking_service.service;

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

    public Passenger savePassenger(
            Passenger passenger,
            String username) {

        passenger.setId(null);
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
            Passenger updatedPassenger,
            String username,
            String role) {

        Passenger existing =
                findOwned(id, username, role);

        existing.setName(updatedPassenger.getName());
        existing.setEmail(updatedPassenger.getEmail());
        existing.setAge(updatedPassenger.getAge());
        existing.setContact(updatedPassenger.getContact());

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