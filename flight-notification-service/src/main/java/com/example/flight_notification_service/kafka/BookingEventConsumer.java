package com.example.flight_notification_service.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class BookingEventConsumer {

    @KafkaListener(
            topics = "booking-created",
            groupId = "notification-service-group"
    )
    public void consumeBookingCreatedEvent(BookingCreatedEvent event) {

        System.out.println("===== BOOKING CREATED EVENT =====");

        System.out.println("Booking ID : " + event.getBookingId());
        System.out.println("Flight ID  : " + event.getFlightId());
        System.out.println("Username   : " + event.getUsername());

        System.out.println("=================================");
    }
}