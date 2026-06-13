package com.microservice.ingestor.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservice.ingestor.config.kafkaConfig;
import com.microservice.job.api.dto.UserDto;
import com.microservice.job.client.AnotherServiceClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Listens to Kafka topics and triggers downstream service calls.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class kafkaConsumer {

    private final ObjectMapper objectMapper;
    private final AnotherServiceClient anotherServiceClient;

    /**
     * Listens to "user.create" topic.
     * Deserializes the payload into UserDto and calls another-service.
     */
    @KafkaListener(topics = kafkaConfig.USER_CREATE_TOPIC, groupId = "ingestor-group")
    public void onUserCreate(String message) {
        log.info("[CONSUMER] Received message from topic '{}': {}", kafkaConfig.USER_CREATE_TOPIC, message);

        try {
            // Deserialize JSON string → UserDto
            UserDto userDto = objectMapper.readValue(message, UserDto.class);
            log.info("[CONSUMER] Parsed payload into UserDto: {}", userDto);

            // Call another-service via HTTP client
            UserDto created = anotherServiceClient.createUser(userDto);
            log.info("[CONSUMER] Successfully created user via another-service: {}",
                    created);

        } catch (JsonProcessingException e) {
            log.error("[CONSUMER] Failed to deserialize message: {}", message, e);
        } catch (Exception e) {
            log.error("[CONSUMER] Failed to call another-service for message: {}", message, e);
        }
    }
}
