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

    public Passenger savePassenger(Passenger passenger) {
        return passengerRepository.save(passenger);
    }

    public List<Passenger> getAllPassengers() {
        return passengerRepository.findAll();
    }

    public Passenger updatePassenger(Long id, Passenger updatedPassenger) {
        updatedPassenger.setId(id);
        return passengerRepository.save(updatedPassenger);
    }
    
    public void deletePassenger(Long id) {
        passengerRepository.deleteById(id);
    }
}
