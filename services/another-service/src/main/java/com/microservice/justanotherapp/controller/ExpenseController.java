package com.microservice.justanotherapp.controller;


import com.microservice.job.api.dto.ExpenseRequestDto;
import com.microservice.job.api.dto.ExpenseResponseDto;
import com.microservice.job.api.dto.ExpenseTotalDto;
import com.microservice.justanotherapp.service.ExpenseService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    // ── POST /api/expenses ────────────────────────────────────────────────
    @PostMapping("/create")
    public ResponseEntity<ExpenseResponseDto> create(
            @Valid @RequestBody ExpenseRequestDto request) {

        log.info("[ExpenseController] POST /api/expenses");
        ExpenseResponseDto created = expenseService.create(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    // ── GET /api/expenses ─────────────────────────────────────────────────
    @GetMapping("/all")
    public ResponseEntity<List<ExpenseResponseDto>> findAll(

            @RequestParam(required = false) Long categoryId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            @PageableDefault(size = 20, sort = "expenseDate", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.info("[ExpenseController] GET /api/expenses — categoryId={} startDate={} endDate={} page={}",
                categoryId, startDate, endDate, pageable.getPageNumber());

        return ResponseEntity.ok(expenseService.findAll(categoryId, startDate, endDate));
    }

    // ── GET /api/expenses/total ───────────────────────────────────────────
    @GetMapping("/total")
    public ResponseEntity<ExpenseTotalDto> getTotal(

            @RequestParam(required = false) Long categoryId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("[ExpenseController] GET /api/expenses/total — categoryId={} startDate={} endDate={}",
                categoryId, startDate, endDate);

        return ResponseEntity.ok(expenseService.getTotal(categoryId, startDate, endDate));
    }
}