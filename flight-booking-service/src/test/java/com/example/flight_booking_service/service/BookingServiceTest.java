package com.example.flight_booking_service.service;

import com.example.flight_booking_service.dto.BookingRequest;
import com.example.flight_booking_service.entity.Booking;
import com.example.flight_booking_service.entity.Passenger;
import com.example.flight_booking_service.exception.AccessDeniedException;
import com.example.flight_booking_service.exception.NoSeatsAvailableException;
import com.example.flight_booking_service.exception.ResourceNotFoundException;
import com.example.flight_booking_service.repository.BookingRepository;
import com.example.flight_booking_service.repository.PassengerRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PassengerRepository passengerRepository;

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new BookingService();

        try {
            var bookingRepositoryField =
                    BookingService.class.getDeclaredField("bookingRepository");

            bookingRepositoryField.setAccessible(true);
            bookingRepositoryField.set(bookingService, bookingRepository);

            var passengerRepositoryField =
                    BookingService.class.getDeclaredField("passengerRepository");

            passengerRepositoryField.setAccessible(true);
            passengerRepositoryField.set(bookingService, passengerRepository);

            var restClientField =
                    BookingService.class.getDeclaredField("restClient");

            restClientField.setAccessible(true);
            restClientField.set(bookingService, restClient);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getAllBookings_shouldReturnAllBookingsForAdmin() {

        List<Booking> bookings = List.of(
                mock(Booking.class),
                mock(Booking.class)
        );

        when(bookingRepository.findAll())
                .thenReturn(bookings);

        List<Booking> result =
                bookingService.getAllBookings(
                        "nikhil",
                        "ROLE_ADMIN"
                );

        assertEquals(bookings, result);

        verify(bookingRepository)
                .findAll();

        verify(bookingRepository, never())
                .findByCreatedBy(anyString());
    }

    @Test
    void getAllBookings_shouldReturnOnlyUserBookings() {

        List<Booking> bookings = List.of(
                mock(Booking.class)
        );

        when(bookingRepository.findByCreatedBy("nikhil"))
                .thenReturn(bookings);

        List<Booking> result =
                bookingService.getAllBookings(
                        "nikhil",
                        "ROLE_USER"
                );

        assertEquals(bookings, result);

        verify(bookingRepository)
                .findByCreatedBy("nikhil");

        verify(bookingRepository, never())
                .findAll();
    }

    @Test
    void createBooking_shouldCreateBookingSuccessfully() {

        BookingRequest request =
                mock(BookingRequest.class);

        Passenger passenger =
                mock(Passenger.class);

        when(request.getPassengerId())
                .thenReturn(1L);

        when(request.getFlightId())
                .thenReturn(10L);

        when(passengerRepository.findById(1L))
                .thenReturn(Optional.of(passenger));

        mockSuccessfulFlightRequest();

        Booking savedBooking =
                mock(Booking.class);

        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(savedBooking);

        Booking result =
                bookingService.createBooking(
                        request,
                        "nikhil"
                );

        assertEquals(savedBooking, result);

        ArgumentCaptor<Booking> captor =
                ArgumentCaptor.forClass(Booking.class);

        verify(bookingRepository)
                .save(captor.capture());

        Booking createdBooking =
                captor.getValue();

        assertEquals(
                10L,
                createdBooking.getFlightId()
        );

        assertEquals(
                passenger,
                createdBooking.getPassenger()
        );

        assertEquals(
                "nikhil",
                createdBooking.getCreatedBy()
        );

        verify(passengerRepository)
                .findById(1L);

        verify(restClient)
                .post();
    }

    @Test
    void createBooking_shouldThrowWhenPassengerDoesNotExist() {

        BookingRequest request =
                mock(BookingRequest.class);

        when(request.getPassengerId())
                .thenReturn(1L);

        when(passengerRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> bookingService.createBooking(
                        request,
                        "nikhil"
                )
        );

        verify(restClient, never())
                .post();

        verify(bookingRepository, never())
                .save(any());
    }

    @Test
    void createBooking_shouldThrowWhenNoSeatsAvailable() {

        BookingRequest request =
                mock(BookingRequest.class);

        Passenger passenger =
                mock(Passenger.class);

        when(request.getPassengerId())
                .thenReturn(1L);

        when(request.getFlightId())
                .thenReturn(10L);

        when(passengerRepository.findById(1L))
                .thenReturn(Optional.of(passenger));

        mockFlightRequestException(
                HttpStatus.BAD_REQUEST
        );

        assertThrows(
                NoSeatsAvailableException.class,
                () -> bookingService.createBooking(
                        request,
                        "nikhil"
                )
        );

        verify(bookingRepository, never())
                .save(any());
    }

    @Test
    void createBooking_shouldThrowWhenFlightDoesNotExist() {

        BookingRequest request =
                mock(BookingRequest.class);

        Passenger passenger =
                mock(Passenger.class);

        when(request.getPassengerId())
                .thenReturn(1L);

        when(request.getFlightId())
                .thenReturn(10L);

        when(passengerRepository.findById(1L))
                .thenReturn(Optional.of(passenger));

        mockFlightRequestException(
                HttpStatus.NOT_FOUND
        );

        assertThrows(
                ResourceNotFoundException.class,
                () -> bookingService.createBooking(
                        request,
                        "nikhil"
                )
        );

        verify(bookingRepository, never())
                .save(any());
    }

    @Test
    void createBooking_shouldReleaseSeatWhenSavingBookingFails() {

        BookingRequest request =
                mock(BookingRequest.class);

        Passenger passenger =
                mock(Passenger.class);

        when(request.getPassengerId())
                .thenReturn(1L);

        when(request.getFlightId())
                .thenReturn(10L);

        when(passengerRepository.findById(1L))
                .thenReturn(Optional.of(passenger));

        mockSuccessfulFlightRequest();

        RuntimeException databaseException =
                new RuntimeException("Database error");

        when(bookingRepository.save(any(Booking.class)))
                .thenThrow(databaseException);

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> bookingService.createBooking(
                                request,
                                "nikhil"
                        )
                );

        assertEquals(
                databaseException,
                exception
        );

        verify(restClient, times(2))
                .post();
    }

    @Test
    void cancelBooking_shouldCancelOwnBooking() {

        Booking booking =
                mock(Booking.class);

        when(booking.getCreatedBy())
                .thenReturn("nikhil");

        when(booking.getFlightId())
                .thenReturn(10L);

        when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        mockSuccessfulFlightRequest();

        bookingService.cancelBooking(
                1L,
                "nikhil",
                "ROLE_USER"
        );

        verify(restClient)
                .post();

        verify(bookingRepository)
                .delete(booking);
    }

    @Test
void cancelBooking_shouldAllowAdminToCancelBooking() {

    Booking booking =
            mock(Booking.class);

    when(booking.getFlightId())
            .thenReturn(10L);

    when(bookingRepository.findById(1L))
            .thenReturn(Optional.of(booking));

    mockSuccessfulFlightRequest();

    bookingService.cancelBooking(
            1L,
            "nikhil",
            "ROLE_ADMIN"
    );

    verify(restClient)
            .post();

    verify(bookingRepository)
            .delete(booking);
}

    @Test
    void cancelBooking_shouldRejectAnotherUsersBooking() {

        Booking booking =
                mock(Booking.class);

        when(booking.getCreatedBy())
                .thenReturn("otheruser");

        when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        assertThrows(
                AccessDeniedException.class,
                () -> bookingService.cancelBooking(
                        1L,
                        "nikhil",
                        "ROLE_USER"
                )
        );

        verify(restClient, never())
                .post();

        verify(bookingRepository, never())
                .delete(any());
    }

    @Test
    void cancelBooking_shouldThrowWhenBookingDoesNotExist() {

        when(bookingRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> bookingService.cancelBooking(
                        1L,
                        "nikhil",
                        "ROLE_USER"
                )
        );

        verify(restClient, never())
                .post();

        verify(bookingRepository, never())
                .delete(any());
    }

    private void mockSuccessfulFlightRequest() {

        when(restClient.post())
                .thenReturn(requestBodyUriSpec);

        when(
                requestBodyUriSpec.uri(
                        anyString(),
                        any(Object[].class)
                )
        ).thenReturn(requestBodySpec);

        when(requestBodySpec.retrieve())
                .thenReturn(responseSpec);

        when(responseSpec.toBodilessEntity())
                .thenReturn(
                        ResponseEntity.ok().build()
                );
    }

    private void mockFlightRequestException(
            HttpStatus status) {

        when(restClient.post())
                .thenReturn(requestBodyUriSpec);

        when(
                requestBodyUriSpec.uri(
                        anyString(),
                        any(Object[].class)
                )
        ).thenReturn(requestBodySpec);

        when(requestBodySpec.retrieve())
                .thenReturn(responseSpec);

        HttpClientErrorException exception =
                HttpClientErrorException.create(
                        status,
                        status.getReasonPhrase(),
                        HttpHeaders.EMPTY,
                        new byte[0],
                        StandardCharsets.UTF_8
                );

        when(responseSpec.toBodilessEntity())
                .thenThrow(exception);
    }
}