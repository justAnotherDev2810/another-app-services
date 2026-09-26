package com.microservice.justanotherapp.service.impl;

import com.microservice.job.common.exception.ResourceNotFoundException;
import com.microservice.job.api.dto.ExpenseDto;
import com.microservice.job.api.dto.ExpenseRequestDto;
import com.microservice.job.api.dto.ExpenseResponseDto;
import com.microservice.job.api.dto.ExpenseTotalDto;
import com.microservice.justanotherapp.entity.Category;
import com.microservice.justanotherapp.entity.Expense;
import com.microservice.justanotherapp.repository.CategoryRepository;
import com.microservice.justanotherapp.repository.ExpenseRepository;
import com.microservice.justanotherapp.repository.UserRepository;
import com.microservice.justanotherapp.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.microservice.justanotherapp.entity.Expense.fromEntity;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public ExpenseResponseDto create(ExpenseRequestDto request) {
        log.info("[ExpenseService] Creating expense for userId={} categoryId={}",
                request.getUserId(), request.getCategoryId());

        // Validate userId exists
        userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        // Validate categoryId exists — also fetch name for response denormalization
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        Expense saved = expenseRepository.save(
                Expense.builder()
                        .userId(request.getUserId())
                        .categoryId(request.getCategoryId())
                        .amount(request.getAmount())
                        .description(request.getDescription())
                        .expenseDate(request.getExpenseDate())
                        .build()
        );

        log.info("[ExpenseService] Created expense id={}", saved.getId());
        return toDto(fromEntity(saved), category.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExpenseResponseDto> findAll(Long categoryId, LocalDate startDate,
                                             LocalDate endDate, Pageable pageable) {
        log.info("[ExpenseService] Listing expenses — categoryId={} startDate={} endDate={}",
                categoryId, startDate, endDate);

        Page<ExpenseDto> allFiltered = expenseRepository
                .findAllFiltered(categoryId, startDate, endDate, pageable);
        return allFiltered
                .map(e -> {
                    String categoryName = categoryRepository.findById(e.getCategoryId())
                            .map(Category::getName)
                            .orElse("Unknown");
                    return toDto(e, categoryName);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseTotalDto getTotal(Long categoryId, LocalDate startDate, LocalDate endDate) {
        log.info("[ExpenseService] Aggregating expenses — categoryId={} startDate={} endDate={}",
                categoryId, startDate, endDate);

        Object[] result = expenseRepository.aggregateFiltered(categoryId, startDate, endDate);

        return ExpenseTotalDto.builder()
                .total((BigDecimal) result[0])
                .count((Long) result[1])
                .build();
    }

    // ── Mapper ────────────────────────────────────────────────────────────
    private ExpenseResponseDto toDto(ExpenseDto e, String categoryName) {
        return ExpenseResponseDto.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .categoryId(e.getCategoryId())
                .categoryName(categoryName)
                .amount(e.getAmount())
                .description(e.getDescription())
                .expenseDate(e.getExpenseDate())
                .createdAt(e.getCreatedAt())
                .build();
    }
}