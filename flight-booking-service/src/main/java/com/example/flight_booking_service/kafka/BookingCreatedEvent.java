package com.example.flight_booking_service.kafka;

public class BookingCreatedEvent {

    private Long bookingId;
    private Long flightId;
    private String username;

    public BookingCreatedEvent() {
    }

    public BookingCreatedEvent(Long bookingId, Long flightId, String username) {
        this.bookingId = bookingId;
        this.flightId = flightId;
        this.username = username;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getFlightId() {
        return flightId;
    }

    public void setFlightId(Long flightId) {
        this.flightId = flightId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}