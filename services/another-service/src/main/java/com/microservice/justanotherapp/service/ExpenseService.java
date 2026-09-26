package com.microservice.justanotherapp.service;


import com.microservice.job.api.dto.ExpenseRequestDto;
import com.microservice.job.api.dto.ExpenseResponseDto;
import com.microservice.job.api.dto.ExpenseTotalDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseService {

    ExpenseResponseDto create(ExpenseRequestDto request);

    List<ExpenseResponseDto> findAll(Long categoryId, LocalDate startDate,
                                     LocalDate endDate);

    ExpenseTotalDto getTotal(Long categoryId, LocalDate startDate, LocalDate endDate);
}