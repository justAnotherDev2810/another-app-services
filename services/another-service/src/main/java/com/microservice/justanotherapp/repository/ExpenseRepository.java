package com.microservice.justanotherapp.repository;

import com.microservice.job.api.dto.ExpenseDto;
import com.microservice.justanotherapp.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    /**
     * Paginated list with optional filters.
     * All filter params are nullable — when null they are ignored (no WHERE clause added).
     * Uses JPQL rather than Criteria API for readability.
     */
    @Query("""
            SELECT new com.microservice.job.api.dto.ExpenseDto(e.id, e.userId, e.categoryId, e.amount, e.description, e.expenseDate, e.createdAt)
            FROM Expense e
            WHERE (:categoryId IS NULL OR e.categoryId = :categoryId)
              AND (:startDate   IS NULL OR e.expenseDate >= :startDate)
              AND (:endDate     IS NULL OR e.expenseDate <= :endDate)
            ORDER BY e.expenseDate DESC, e.createdAt DESC
            """)
    Page<ExpenseDto> findAllFiltered(
            @Param("categoryId") Long categoryId,
            @Param("startDate")  LocalDate startDate,
            @Param("endDate")    LocalDate endDate,
            Pageable pageable
    );

    /**
     * Aggregate query — SUM and COUNT with same filters as the list endpoint.
     * Runs a single SQL query, never loads rows into memory.
     * Returns Object[] { BigDecimal total, Long count }.
     */
    @Query("""
            SELECT COALESCE(SUM(e.amount), 0), COUNT(e)
            FROM Expense e
            WHERE (:categoryId IS NULL OR e.categoryId = :categoryId)
              AND (:startDate   IS NULL OR e.expenseDate >= :startDate)
              AND (:endDate     IS NULL OR e.expenseDate <= :endDate)
            """)
    Object[] aggregateFiltered(
            @Param("categoryId") Long categoryId,
            @Param("startDate")  LocalDate startDate,
            @Param("endDate")    LocalDate endDate
    );
}