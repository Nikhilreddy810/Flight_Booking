package com.example.flight_booking_service.service;

import com.example.flight_booking_service.entity.Passenger;
import com.example.flight_booking_service.exception.AccessDeniedException;
import com.example.flight_booking_service.exception.ResourceNotFoundException;
import com.example.flight_booking_service.repository.PassengerRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PassengerServiceTest {

    @Mock
    private PassengerRepository passengerRepository;

    @InjectMocks
    private PassengerService passengerService;

    private Passenger passenger;

    @BeforeEach
    void setUp() {

        passenger = new Passenger();

        passenger.setId(1L);
        passenger.setName("Nikhil");
        passenger.setEmail("nikhil@example.com");
        passenger.setAge(21);
        passenger.setContact("9876543210");
        passenger.setCreatedBy("Nikhil");
    }

    @Test
    void savePassenger_success() {

        Passenger newPassenger = new Passenger();

        newPassenger.setId(99L);
        newPassenger.setName("Rahul");
        newPassenger.setEmail("rahul@example.com");
        newPassenger.setAge(25);
        newPassenger.setContact("9876543211");
        newPassenger.setCreatedBy("someone");

        when(passengerRepository.save(any(Passenger.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        Passenger result =
                passengerService.savePassenger(
                        newPassenger,
                        "Nikhil"
                );

        assertNull(result.getId());

        assertEquals(
                "Nikhil",
                result.getCreatedBy()
        );

        assertEquals(
                "Rahul",
                result.getName()
        );

        verify(passengerRepository)
                .save(newPassenger);
    }

    @Test
    void getAllPassengers_user_returnsOwnPassengers() {

        when(passengerRepository.findByCreatedBy("Nikhil"))
                .thenReturn(List.of(passenger));

        List<Passenger> result =
                passengerService.getAllPassengers(
                        "Nikhil",
                        "ROLE_USER"
                );

        assertEquals(1, result.size());

        assertEquals(
                "Nikhil",
                result.get(0).getCreatedBy()
        );

        verify(passengerRepository)
                .findByCreatedBy("Nikhil");

        verify(passengerRepository, never())
                .findAll();
    }

    @Test
    void getAllPassengers_admin_returnsAllPassengers() {

        when(passengerRepository.findAll())
                .thenReturn(List.of(passenger));

        List<Passenger> result =
                passengerService.getAllPassengers(
                        "Admin",
                        "ROLE_ADMIN"
                );

        assertEquals(1, result.size());

        verify(passengerRepository)
                .findAll();

        verify(passengerRepository, never())
                .findByCreatedBy(anyString());
    }

    @Test
    void updatePassenger_success_owner() {

        Passenger updatedPassenger = new Passenger();

        updatedPassenger.setName("Nikhil Updated");
        updatedPassenger.setEmail("updated@example.com");
        updatedPassenger.setAge(22);
        updatedPassenger.setContact("9999999999");

        when(passengerRepository.findById(1L))
                .thenReturn(Optional.of(passenger));

        when(passengerRepository.save(any(Passenger.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        Passenger result =
                passengerService.updatePassenger(
                        1L,
                        updatedPassenger,
                        "Nikhil",
                        "ROLE_USER"
                );

        assertEquals(
                "Nikhil Updated",
                result.getName()
        );

        assertEquals(
                "updated@example.com",
                result.getEmail()
        );

        assertEquals(
                22,
                result.getAge()
        );

        assertEquals(
                "9999999999",
                result.getContact()
        );

        assertEquals(
                "Nikhil",
                result.getCreatedBy()
        );

        verify(passengerRepository)
                .findById(1L);

        verify(passengerRepository)
                .save(passenger);
    }

    @Test
    void updatePassenger_notOwner_throwsAccessDeniedException() {

        Passenger updatedPassenger = new Passenger();

        updatedPassenger.setName("Updated");

        when(passengerRepository.findById(1L))
                .thenReturn(Optional.of(passenger));

        assertThrows(
                AccessDeniedException.class,
                () -> passengerService.updatePassenger(
                        1L,
                        updatedPassenger,
                        "UserB",
                        "ROLE_USER"
                )
        );

        verify(passengerRepository)
                .findById(1L);

        verify(passengerRepository, never())
                .save(any(Passenger.class));
    }

    @Test
    void updatePassenger_admin_canUpdate() {

        Passenger updatedPassenger = new Passenger();

        updatedPassenger.setName("Admin Updated");
        updatedPassenger.setEmail("admin@example.com");
        updatedPassenger.setAge(30);
        updatedPassenger.setContact("8888888888");

        when(passengerRepository.findById(1L))
                .thenReturn(Optional.of(passenger));

        when(passengerRepository.save(any(Passenger.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        Passenger result =
                passengerService.updatePassenger(
                        1L,
                        updatedPassenger,
                        "Admin",
                        "ROLE_ADMIN"
                );

        assertEquals(
                "Admin Updated",
                result.getName()
        );

        verify(passengerRepository)
                .save(passenger);
    }

    @Test
    void updatePassenger_notFound_throwsException() {

        Passenger updatedPassenger = new Passenger();

        updatedPassenger.setName("Updated");

        when(passengerRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> passengerService.updatePassenger(
                        99L,
                        updatedPassenger,
                        "Nikhil",
                        "ROLE_USER"
                )
        );

        verify(passengerRepository)
                .findById(99L);

        verify(passengerRepository, never())
                .save(any(Passenger.class));
    }

    @Test
    void deletePassenger_success_owner() {

        when(passengerRepository.findById(1L))
                .thenReturn(Optional.of(passenger));

        passengerService.deletePassenger(
                1L,
                "Nikhil",
                "ROLE_USER"
        );

        verify(passengerRepository)
                .findById(1L);

        verify(passengerRepository)
                .delete(passenger);
    }

    @Test
    void deletePassenger_notOwner_throwsAccessDeniedException() {

        when(passengerRepository.findById(1L))
                .thenReturn(Optional.of(passenger));

        assertThrows(
                AccessDeniedException.class,
                () -> passengerService.deletePassenger(
                        1L,
                        "UserB",
                        "ROLE_USER"
                )
        );

        verify(passengerRepository)
                .findById(1L);

        verify(passengerRepository, never())
                .delete(any(Passenger.class));
    }

    @Test
    void deletePassenger_admin_canDelete() {

        when(passengerRepository.findById(1L))
                .thenReturn(Optional.of(passenger));

        passengerService.deletePassenger(
                1L,
                "Admin",
                "ROLE_ADMIN"
        );

        verify(passengerRepository)
                .delete(passenger);
    }

    @Test
    void deletePassenger_notFound_throwsException() {

        when(passengerRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> passengerService.deletePassenger(
                        99L,
                        "Nikhil",
                        "ROLE_USER"
                )
        );

        verify(passengerRepository, never())
                .delete(any(Passenger.class));
    }
}