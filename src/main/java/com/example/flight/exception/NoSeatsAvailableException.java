package com.example.flight.exception;

public class NoSeatsAvailableException extends RuntimeException {

    public NoSeatsAvailableException(String message) {
        super(message);
    }
}
