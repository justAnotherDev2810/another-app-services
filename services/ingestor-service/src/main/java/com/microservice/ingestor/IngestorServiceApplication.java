package com.microservice.ingestor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.microservice.job.client")
public class IngestorServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(IngestorServiceApplication.class, args);
    }
}