package com.microservice.ingestor.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.microservice.ingestor.dto.UserDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * HTTP client for calling another-service's REST API.
 * Uses WebClient (non-blocking) — Spring Boot 3.x recommended approach.
 *
 * Base URL is configured in application.yml so it's easy to change
 * per environment without touching code.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnotherServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${clients.another-service.url}")
    private String anotherServiceBaseUrl;

    /**
     * Calls POST /api/user on another-service.
     * Blocks until response — acceptable for now since we're in a Kafka listener.
     */
    public UserDto createUser(UserDto userDto) {
        log.info("[CLIENT] Calling another-service POST /api/user with payload: {}", userDto);

        UserDto response = webClientBuilder
                .baseUrl(anotherServiceBaseUrl)
                .build()
                .post()
                .uri("/api/user")
                .bodyValue(userDto)
                .retrieve()
                .bodyToMono(UserDto.class)
                .block(); // blocking is fine here — we're already on a Kafka listener thread

        log.info("[CLIENT] another-service responded with: {}", response);
        return response;
    }
}