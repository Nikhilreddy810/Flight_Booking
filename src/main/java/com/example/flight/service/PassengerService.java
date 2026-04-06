package com.example.flight.service;

import com.example.flight.entity.Passenger;
import com.example.flight.repository.PassengerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PassengerService {

    @Autowired
    private PassengerRepository passengerRepository;

    public Passenger savePassenger(Passenger passenger, String username) {
        passenger.setId(null);
        passenger.setCreatedBy(username);
        return passengerRepository.save(passenger);
    }

    public List<Passenger> getAllPassengers(String username, String role) {
        if (role.equals("ROLE_ADMIN")) {
            return passengerRepository.findAll();
        }
        return passengerRepository.findByCreatedBy(username);
    }

    public Passenger updatePassenger(Long id, Passenger updatedPassenger) {
        updatedPassenger.setId(id);
        return passengerRepository.save(updatedPassenger);
    }

    public void deletePassenger(Long id) {
        passengerRepository.deleteById(id);
    }
}