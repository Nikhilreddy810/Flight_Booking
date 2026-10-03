package com.example.flight_booking_service.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class BookingEventProducer {

    private static final String TOPIC = "booking-created";

    private final KafkaTemplate<String, BookingCreatedEvent> kafkaTemplate;

    public BookingEventProducer(
            KafkaTemplate<String, BookingCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendBookingCreatedEvent(BookingCreatedEvent event) {
        kafkaTemplate.send(TOPIC, event);
    }
}