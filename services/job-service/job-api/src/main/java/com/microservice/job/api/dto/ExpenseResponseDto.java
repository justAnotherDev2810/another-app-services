package com.microservice.job.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Expense response payload")
public class ExpenseResponseDto {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "1")
    private Long userId;

    @Schema(example = "2")
    private Long categoryId;

    @Schema(example = "Food")
    private String categoryName;  // denormalized for convenience — avoids extra call from frontend

    @Schema(example = "129.99")
    private BigDecimal amount;

    @Schema(example = "Dinner with client")
    private String description;

    @Schema(example = "2026-09-15")
    private LocalDate expenseDate;

    private LocalDateTime createdAt;
}