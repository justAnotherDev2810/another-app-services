package com.microservice.justanotherapp.controller;

import com.microservice.job.api.dto.CategoryDto;
import com.microservice.justanotherapp.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * GET /api/categories
     * Returns the full list of categories — no pagination needed,
     * this is a small fixed-ish list seeded via Flyway.
     */
    @GetMapping("/all")
    public ResponseEntity<List<CategoryDto>> getAll() {
        log.info("[CategoryController] GET /api/categories");
        return ResponseEntity.ok(categoryService.getAllCategories());
    }
}