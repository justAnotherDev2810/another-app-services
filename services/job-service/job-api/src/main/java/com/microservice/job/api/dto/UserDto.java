package com.microservice.job.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String role;
    private String status;         // "Active" | "Inactive" | "Pending"
    private String avatarUrl;      // nullable, optional
    private LocalDateTime lastLogin;   // nullable, set on auth
    private LocalDateTime createdAt;

}

