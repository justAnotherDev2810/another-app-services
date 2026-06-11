package com.microservice.ingestor.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Defines Kafka topics as Spring beans.
 * Spring will auto-create these topics on the broker when the app starts
 * if they don't already exist.
 */
@Configuration
public class kafkaConfig {
    // Topic name constant — used by both producer and consumer
    // so there's no magic string duplication
    public static final String USER_CREATE_TOPIC = "user.create";

    @Bean
    public NewTopic userCreateTopic() {
        return TopicBuilder
                .name(USER_CREATE_TOPIC)
                .partitions(1) // single partition for now — fine for local dev
                .replicas(1) // single replica — fine for local single-node Kafka
                .build();
    }
}
