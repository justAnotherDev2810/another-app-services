package com.microservice.job.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Inbound payload for POST /api/expenses.
 * Jakarta Validation annotations drive the constraint checks —
 * GlobalExceptionHandler catches MethodArgumentNotValidException
 * and returns the standard ApiErrorResponse shape.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseRequestDto {

    @NotNull(message = "userId is required")
    private Long userId;

    @NotNull(message = "categoryId is required")
    private Long categoryId;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be greater than 0")
    @Digits(integer = 15, fraction = 4, message = "amount must have at most 15 integer digits and 4 decimal places")
    private BigDecimal amount;

    @Size(max = 500, message = "description must not exceed 500 characters")
    private String description;

    @NotNull(message = "expenseDate is required")
    private LocalDate expenseDate;
}