package com.microservice.justanotherapp.dto;

import com.microservice.justanotherapp.entity.User;
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

    public static UserDto fromEntity(User user) {
        if (user == null)
            return null;
        return UserDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .username(user.getUserName())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .avatarUrl(user.getAvatarUrl())
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public User toEntity() {
        return User.builder()
                .id(this.id)
                .firstName(this.firstName)
                .lastName(this.lastName)
                .userName(this.username)
                .email(this.email)
                .role(this.role)
                .status(this.status)
                .avatarUrl(this.avatarUrl)
                .lastLogin(this.lastLogin)
                .createdAt(this.createdAt)
                .build();
    }
}
