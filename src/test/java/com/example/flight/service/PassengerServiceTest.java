package com.example.flight.service;

import com.example.flight.entity.Passenger;
import com.example.flight.exception.ResourceNotFoundException;
import com.example.flight.repository.PassengerRepository;
import com.example.flight.security.Roles;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PassengerServiceTest {

    @Mock
    private PassengerRepository passengerRepository;

    @InjectMocks
    private PassengerService passengerService;

    private Passenger stored;

    @BeforeEach
    void setUp() {
        stored = new Passenger();
        stored.setId(1L);
        stored.setName("Nikhil");
        stored.setEmail("nikhil@example.com");
        stored.setAge(25);
        stored.setContact("9999999999");
        stored.setCreatedBy("owner");
    }

    @Test
    void shouldStampCurrentUser_andIgnoreClientSuppliedId() {
        Passenger incoming = new Passenger();
        incoming.setId(99L);
        incoming.setName("New");
        when(passengerRepository.save(any(Passenger.class))).thenAnswer(i -> i.getArgument(0));

        Passenger saved = passengerService.savePassenger(incoming, "owner");

        assertNull(saved.getId(), "a client-supplied id must not overwrite an existing row");
        assertEquals("owner", saved.getCreatedBy());
    }

    @Test
    void shouldReturnAllPassengers_forAdmin() {
        when(passengerRepository.findAll()).thenReturn(List.of(stored));

        passengerService.getAllPassengers("admin", Roles.ADMIN);

        verify(passengerRepository).findAll();
        verify(passengerRepository, never()).findByCreatedBy(anyString());
    }

    @Test
    void shouldReturnOnlyOwnPassengers_forUser() {
        when(passengerRepository.findByCreatedBy("owner")).thenReturn(List.of(stored));

        passengerService.getAllPassengers("owner", Roles.USER);

        verify(passengerRepository).findByCreatedBy("owner");
        verify(passengerRepository, never()).findAll();
    }

    @Test
    void shouldUpdatePassenger_andPreserveOwner() {
        Passenger incoming = new Passenger();
        incoming.setName("Updated");
        incoming.setEmail("updated@example.com");
        incoming.setAge(30);
        incoming.setContact("8888888888");

        when(passengerRepository.findById(1L)).thenReturn(Optional.of(stored));
        when(passengerRepository.save(any(Passenger.class))).thenAnswer(i -> i.getArgument(0));

        Passenger result = passengerService.updatePassenger(1L, incoming, "owner", Roles.USER);

        assertEquals("Updated", result.getName());
        assertEquals(1L, result.getId());
        assertEquals("owner", result.getCreatedBy(), "update must not wipe createdBy");
    }

    @Test
    void shouldDenyUpdate_whenPassengerBelongsToAnotherUser() {
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(stored));

        assertThrows(AccessDeniedException.class, () -> {
            passengerService.updatePassenger(1L, new Passenger(), "intruder", Roles.USER);
        });

        verify(passengerRepository, never()).save(any(Passenger.class));
    }

    @Test
    void shouldDenyDelete_whenPassengerBelongsToAnotherUser() {
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(stored));

        assertThrows(AccessDeniedException.class, () -> {
            passengerService.deletePassenger(1L, "intruder", Roles.USER);
        });

        verify(passengerRepository, never()).delete(any(Passenger.class));
    }

    @Test
    void shouldAllowAdminToDeleteAnyPassenger() {
        when(passengerRepository.findById(1L)).thenReturn(Optional.of(stored));

        passengerService.deletePassenger(1L, "admin", Roles.ADMIN);

        verify(passengerRepository).delete(stored);
    }

    @Test
    void shouldThrowNotFound_whenDeletingMissingPassenger() {
        when(passengerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            passengerService.deletePassenger(1L, "owner", Roles.USER);
        });
    }
}
