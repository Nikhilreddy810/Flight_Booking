package com.example.flight_booking_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class BookingRequest {

    @NotNull(message = "Flight ID is required")
    @Positive(message = "Flight ID must be greater than 0")
    private Long flightId;

    @NotNull(message = "Passenger ID is required")
    @Positive(message = "Passenger ID must be greater than 0")
    private Long passengerId;

    public BookingRequest() {
    }

    public Long getFlightId() {
        return flightId;
    }

    public void setFlightId(Long flightId) {
        this.flightId = flightId;
    }

    public Long getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(Long passengerId) {
        this.passengerId = passengerId;
    }
}