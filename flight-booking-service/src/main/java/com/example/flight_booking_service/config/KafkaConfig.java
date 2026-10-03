package com.example.flight_booking_service.config;

import com.example.flight_booking_service.kafka.BookingCreatedEvent;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.StringSerializer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public Map<String, Object> producerConfigs() {

        Map<String, Object> props = new HashMap<>();

        props.put(
                org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        props.put(
                org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );

        props.put(
                org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JsonSerializer.class
        );

        return props;
    }

    @Bean
    public DefaultKafkaProducerFactory<String, BookingCreatedEvent>
    producerFactory() {

        return new DefaultKafkaProducerFactory<>(
                producerConfigs()
        );
    }

    @Bean
    public KafkaTemplate<String, BookingCreatedEvent> kafkaTemplate() {

        return new KafkaTemplate<>(
                producerFactory()
        );
    }

    @Bean
    public NewTopic bookingCreatedTopic() {

        return new NewTopic(
                "booking-created",
                1,
                (short) 1
        );
    }
}