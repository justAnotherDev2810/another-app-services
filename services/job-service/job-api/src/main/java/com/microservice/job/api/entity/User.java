package com.microservice.job.api.entity;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private Long id;
    private String firstName;
    private String lastName;
    private String userName;
    private String email;
    private String role;
    private LocalDateTime createdAt = LocalDateTime.now();
}