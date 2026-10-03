package com.example.flight_booking_service.service;

import com.example.flight_booking_service.dto.PassengerRequest;
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
import static org.mockito.ArgumentMatchers.*;
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
    void createPassenger_success() {

        PassengerRequest request =
                new PassengerRequest();

        request.setName("Rahul");
        request.setEmail("rahul@example.com");
        request.setAge(25);
        request.setContact("9876543211");

        when(passengerRepository.save(any(Passenger.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        Passenger result =
                passengerService.createPassenger(
                        request,
                        "Nikhil"
                );

        assertEquals(
                "Rahul",
                result.getName()
        );

        assertEquals(
                "rahul@example.com",
                result.getEmail()
        );

        assertEquals(
                25,
                result.getAge()
        );

        assertEquals(
                "9876543211",
                result.getContact()
        );

        assertEquals(
                "Nikhil",
                result.getCreatedBy()
        );

        verify(passengerRepository)
                .save(any(Passenger.class));
    }

    @Test
    void getAllPassengers_user_returnsOwnPassengers() {

        when(
                passengerRepository.findByCreatedBy("Nikhil")
        ).thenReturn(List.of(passenger));

        List<Passenger> result =
                passengerService.getAllPassengers(
                        "Nikhil",
                        "ROLE_USER"
                );

        assertEquals(
                1,
                result.size()
        );

        verify(
                passengerRepository
        ).findByCreatedBy("Nikhil");

        verify(
                passengerRepository,
                never()
        ).findAll();
    }

    @Test
    void getAllPassengers_admin_returnsAllPassengers() {

        when(
                passengerRepository.findAll()
        ).thenReturn(List.of(passenger));

        List<Passenger> result =
                passengerService.getAllPassengers(
                        "Admin",
                        "ROLE_ADMIN"
                );

        assertEquals(
                1,
                result.size()
        );

        verify(
                passengerRepository
        ).findAll();

        verify(
                passengerRepository,
                never()
        ).findByCreatedBy(anyString());
    }

    @Test
    void updatePassenger_success_owner() {

        PassengerRequest request =
                new PassengerRequest();

        request.setName("Nikhil Updated");
        request.setEmail("updated@example.com");
        request.setAge(22);
        request.setContact("9999999999");

        when(
                passengerRepository.findById(1L)
        ).thenReturn(Optional.of(passenger));

        when(
                passengerRepository.save(any(Passenger.class))
        ).thenAnswer(invocation ->
                invocation.getArgument(0));

        Passenger result =
                passengerService.updatePassenger(
                        1L,
                        request,
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

        verify(
                passengerRepository
        ).save(passenger);
    }

    @Test
    void updatePassenger_notOwner_throwsAccessDeniedException() {

        PassengerRequest request =
                new PassengerRequest();

        request.setName("Updated");

        when(
                passengerRepository.findById(1L)
        ).thenReturn(Optional.of(passenger));

        assertThrows(
                AccessDeniedException.class,
                () ->
                        passengerService.updatePassenger(
                                1L,
                                request,
                                "UserB",
                                "ROLE_USER"
                        )
        );

        verify(
                passengerRepository,
                never()
        ).save(any(Passenger.class));
    }

    @Test
    void updatePassenger_admin_canUpdate() {

        PassengerRequest request =
                new PassengerRequest();

        request.setName("Admin Updated");
        request.setEmail("admin@example.com");
        request.setAge(30);
        request.setContact("8888888888");

        when(
                passengerRepository.findById(1L)
        ).thenReturn(Optional.of(passenger));

        when(
                passengerRepository.save(any(Passenger.class))
        ).thenAnswer(invocation ->
                invocation.getArgument(0));

        Passenger result =
                passengerService.updatePassenger(
                        1L,
                        request,
                        "Admin",
                        "ROLE_ADMIN"
                );

        assertEquals(
                "Admin Updated",
                result.getName()
        );

        verify(
                passengerRepository
        ).save(passenger);
    }

    @Test
    void updatePassenger_notFound_throwsException() {

        PassengerRequest request =
                new PassengerRequest();

        request.setName("Updated");

        when(
                passengerRepository.findById(99L)
        ).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () ->
                        passengerService.updatePassenger(
                                99L,
                                request,
                                "Nikhil",
                                "ROLE_USER"
                        )
        );

        verify(
                passengerRepository,
                never()
        ).save(any(Passenger.class));
    }

    @Test
    void deletePassenger_success_owner() {

        when(
                passengerRepository.findById(1L)
        ).thenReturn(Optional.of(passenger));

        passengerService.deletePassenger(
                1L,
                "Nikhil",
                "ROLE_USER"
        );

        verify(
                passengerRepository
        ).delete(passenger);
    }

    @Test
    void deletePassenger_notOwner_throwsAccessDeniedException() {

        when(
                passengerRepository.findById(1L)
        ).thenReturn(Optional.of(passenger));

        assertThrows(
                AccessDeniedException.class,
                () ->
                        passengerService.deletePassenger(
                                1L,
                                "UserB",
                                "ROLE_USER"
                        )
        );

        verify(
                passengerRepository,
                never()
        ).delete(any(Passenger.class));
    }

    @Test
    void deletePassenger_admin_canDelete() {

        when(
                passengerRepository.findById(1L)
        ).thenReturn(Optional.of(passenger));

        passengerService.deletePassenger(
                1L,
                "Admin",
                "ROLE_ADMIN"
        );

        verify(
                passengerRepository
        ).delete(passenger);
    }

    @Test
    void deletePassenger_notFound_throwsException() {

        when(
                passengerRepository.findById(99L)
        ).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () ->
                        passengerService.deletePassenger(
                                99L,
                                "Nikhil",
                                "ROLE_USER"
                        )
        );

        verify(
                passengerRepository,
                never()
        ).delete(any(Passenger.class));
    }
}