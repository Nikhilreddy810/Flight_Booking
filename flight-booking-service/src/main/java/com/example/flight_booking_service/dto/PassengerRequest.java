package com.example.flight_booking_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PassengerRequest {

    @NotBlank(message = "Name cannot be empty")
    @Size(
            min = 2,
            max = 100,
            message = "Name must be between 2 and 100 characters"
    )
    private String name;

    @NotBlank(message = "Email cannot be empty")
    @Size(
            max = 150,
            message = "Email cannot exceed 150 characters"
    )
    @Email(message = "Invalid email format")
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "Email must be in a valid format"
    )
    private String email;

    @NotNull(message = "Age is required")
    @Min(
            value = 1,
            message = "Age must be at least 1"
    )
    @Max(
            value = 120,
            message = "Age cannot be greater than 120"
    )
    private Integer age;

    @NotBlank(message = "Contact cannot be empty")
    @Pattern(
            regexp = "^[0-9]{10}$",
            message = "Contact must contain exactly 10 digits"
    )
    private String contact;

    public PassengerRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }
}