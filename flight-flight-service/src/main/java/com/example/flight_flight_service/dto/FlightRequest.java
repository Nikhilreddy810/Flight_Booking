package com.example.flight_flight_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class FlightRequest {

    @NotBlank(message = "Flight number cannot be empty")
    @Size(
            max = 50,
            message = "Flight number cannot exceed 50 characters"
    )
    private String flightNumber;

    @NotBlank(message = "Airline cannot be empty")
    @Size(
            max = 100,
            message = "Airline cannot exceed 100 characters"
    )
    private String airline;

    @NotBlank(message = "Source cannot be empty")
    @Size(
            max = 100,
            message = "Source cannot exceed 100 characters"
    )
    private String source;

    @NotBlank(message = "Destination cannot be empty")
    @Size(
            max = 100,
            message = "Destination cannot exceed 100 characters"
    )
    private String destination;

    @Min(
            value = 1,
            message = "Total seats must be greater than 0"
    )
    private int totalSeats;

    @DecimalMin(
            value = "0.0",
            inclusive = true,
            message = "Price cannot be negative"
    )
    private double price;

    public FlightRequest() {
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public String getAirline() {
        return airline;
    }

    public void setAirline(String airline) {
        this.airline = airline;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}