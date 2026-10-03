package com.example.flight_booking_service.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;

import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(
                    GlobalExceptionHandler.class
            );

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>>
    handleResourceNotFound(
            ResourceNotFoundException ex) {

        return build(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
    }

    @ExceptionHandler(NoSeatsAvailableException.class)
    public ResponseEntity<Map<String, Object>>
    handleNoSeats(
            NoSeatsAvailableException ex) {

        return build(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>>
    handleAccessDenied(
            AccessDeniedException ex) {

        return build(
                HttpStatus.FORBIDDEN,
                ex.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>>
    handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors =
                new LinkedHashMap<>();

        for (FieldError error :
                ex.getBindingResult().getFieldErrors()) {

            fieldErrors.put(
                    error.getField(),
                    error.getDefaultMessage()
            );
        }

        Map<String, Object> response =
                body(
                        HttpStatus.BAD_REQUEST,
                        "Validation failed"
                );

        response.put(
                "errors",
                fieldErrors
        );

        return new ResponseEntity<>(
                response,
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(
            HttpMessageNotReadableException.class
    )
    public ResponseEntity<Map<String, Object>>
    handleUnreadableBody(
            HttpMessageNotReadableException ex) {

        return build(
                HttpStatus.BAD_REQUEST,
                "Malformed or invalid request body"
        );
    }

    @ExceptionHandler(
            DataIntegrityViolationException.class
    )
    public ResponseEntity<Map<String, Object>>
    handleDataIntegrityViolation(
            DataIntegrityViolationException ex) {

        log.warn(
                "Data integrity violation",
                ex
        );

        return build(
                HttpStatus.CONFLICT,
                "Request conflicts with existing data"
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>>
    handleGenericException(Exception ex) {

        log.error(
                "Unhandled exception",
                ex
        );

        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error"
        );
    }

    private ResponseEntity<Map<String, Object>>
    build(
            HttpStatus status,
            String message) {

        return new ResponseEntity<>(
                body(status, message),
                status
        );
    }

    private Map<String, Object>
    body(
            HttpStatus status,
            String message) {

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "status",
                status.value()
        );

        response.put(
                "message",
                message
        );

        return response;
    }
}