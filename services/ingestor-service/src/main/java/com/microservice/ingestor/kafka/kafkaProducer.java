package com.microservice.ingestor.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservice.ingestor.config.kafkaConfig;
import com.microservice.ingestor.dto.CloudEventDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Publishes events to Kafka topics.
 * Reads the "type" field from the CloudEvent to resolve which topic to publish
 * to.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class kafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Resolves the Kafka topic from CloudEvent type and publishes the data payload.
     *
     * CloudEvent type "com.microservice.user.create" → topic "user.create"
     * 
     * @throws Exception
     */
    public void publish(CloudEventDto event) throws Exception {
        String topic = resolveTopic(event.getType());
        String id = event.getId();

        if (id == null) {
            throw new Exception("No Id provided");
        } else if (topic == null) {
            throw new Exception("No topic found for event type");
        }

        // Serialize only the "data" field — that's what the consumer needs
        String payload = objectMapper.writeValueAsString(event.getData());

        log.info("[PRODUCER] Publishing to topic '{}' | eventId='{}' | source='{}'",
                topic, id, event.getSource());

        kafkaTemplate.send(topic, id, payload);

        log.info("[PRODUCER] Successfully published to topic '{}'", topic);
    }

    /**
     * Maps CloudEvent type to a Kafka topic name.
     * Pattern: "com.microservice.user.create" → last two segments → "user.create"
     *
     * Add more mappings here as you add more event types.
     */
    private String resolveTopic(String eventType) {
        return switch (eventType) {
            case "com.microservice.user.create" -> kafkaConfig.USER_CREATE_TOPIC;
            default -> throw new IllegalArgumentException(
                    "Unknown event type, no topic mapping found: " + eventType);
        };
    }
}
