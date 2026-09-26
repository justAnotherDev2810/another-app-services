package com.microservice.justanotherapp.entity;

import com.microservice.job.api.dto.ExpenseDto;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Expense entity.
 *
 * Design notes:
 * - amount is BigDecimal, never float/double — financial values require
 *   exact decimal arithmetic; floats introduce rounding errors
 * - expenseDate is LocalDate (date only) — no time component needed for
 *   expense tracking; keeps queries and display simpler
 * - userId and categoryId are stored as plain Long FKs — no @ManyToOne
 *   join here intentionally; avoids N+1 issues and keeps the entity lean.
 *   If you need the full User/Category object later, add a join query
 *   in the repository rather than making the entity eager-load relations.
 */
@Entity
@Table(name = "expenses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(length = 500)
    private String description;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public static ExpenseDto fromEntity(Expense expense) {
        if (expense == null) return null;
        return ExpenseDto.builder()
                .id(expense.getId())
                .userId(expense.getUserId())
                .categoryId(expense.getCategoryId())
                .amount(expense.getAmount())
                .description(expense.getDescription())
                .expenseDate(expense.getExpenseDate())
                .createdAt(expense.getCreatedAt())
                .build();
    }

    public static Expense toEntity(ExpenseDto dto) {
        if (dto == null) return null;
        return Expense.builder()
                .id(dto.getId())
                .userId(dto.getUserId())
                .categoryId(dto.getCategoryId())
                .amount(dto.getAmount())
                .description(dto.getDescription())
                .expenseDate(dto.getExpenseDate())
                .createdAt(dto.getCreatedAt())
                .build();
    }
}