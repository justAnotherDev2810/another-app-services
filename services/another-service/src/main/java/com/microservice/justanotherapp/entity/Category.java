package com.microservice.justanotherapp.entity;

import com.microservice.job.api.dto.CategoryDto;
import jakarta.persistence.*;
import lombok.*;

/**
 * Category entity — represents a user-defined or system-defined
 * category for grouping items (expenses, tasks, etc).
 *
 * Kept intentionally lean — id + name only.
 * Additional fields (description, icon, colour) can be added
 * via future Flyway migrations without touching existing data.
 */
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    public static CategoryDto fromEntity(Category category) {
        if (category == null) return null;
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    public static Category toEntity(CategoryDto dto) {
        if (dto == null) return null;
        return Category.builder()
                .id(dto.getId())
                .name(dto.getName())
                .build();
    }
}