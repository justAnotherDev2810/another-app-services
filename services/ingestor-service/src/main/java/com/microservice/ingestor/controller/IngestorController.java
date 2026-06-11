package com.microservice.ingestor.controller;

import com.microservice.ingestor.dto.CloudEventDto;
import com.microservice.ingestor.kafka.kafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Entry point for the ingestor service.
 * Accepts CloudEvent payloads and hands them off to the Kafka producer.
 *
 * This is what Postman (or a real connect flow) calls.
 */
@Slf4j
@RestController
@RequestMapping("/ingest")
@RequiredArgsConstructor
public class IngestorController {

    private final kafkaProducer kafkaProducer;

    /**
     * POST /ingest
     *
     * Accepts a CloudEvent envelope, publishes data to the resolved Kafka topic.
     * The Kafka consumer in this same service picks it up and calls
     * another-service.
     *
     * Example request body:
     * {
     * "specversion": "1.0",
     * "type": "com.microservice.user.create",
     * "source": "/connect-flow/user",
     * "id": "abc-123",
     * "datacontenttype": "application/json",
     * "data": {
     * "firstName": "Tejas",
     * "lastName": "Nambiar",
     * "username": "tejas123",
     * "email": "tejas@email.com",
     * "role": "ADMIN"
     * }
     * }
     */
    @PostMapping
    public ResponseEntity<String> ingest(@RequestBody CloudEventDto event) {
        log.info("[CONTROLLER] Received CloudEvent | id='{}' | type='{}' | source='{}'",
                event.getId(), event.getType(), event.getSource());
        try {
            kafkaProducer.publish(event);
            return ResponseEntity.accepted().body("Event accepted and published to Kafka");
        } catch (IllegalArgumentException e) {
            log.error("[CONTROLLER] Unknown event type: {}", event.getType());
            return ResponseEntity.badRequest().body("Unknown event type: " + event.getType());
        } catch (Exception e) {
            log.error("[CONTROLLER] Failed to publish event: {}", event.getId(), e);
            return ResponseEntity.internalServerError().body("Failed to process event");
        }
    }
}