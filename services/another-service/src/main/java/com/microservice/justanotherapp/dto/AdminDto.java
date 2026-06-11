package com.microservice.justanotherapp.dto;

import com.microservice.justanotherapp.entity.Admin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDto {
    private Long id;
    private String username;
    private String email;
    private String role;

    public static AdminDto fromEntity(Admin admin) {
        if (admin == null) return null;
        return AdminDto.builder()
                .id(admin.getId())
                .username(admin.getUsername())
                .email(admin.getEmail())
                .role(admin.getRole())
                .build();
    }

    public Admin toEntity() {
        return Admin.builder()
                .id(this.id)
                .username(this.username)
                .email(this.email)
                .role(this.role)
                .build();
    }
}

