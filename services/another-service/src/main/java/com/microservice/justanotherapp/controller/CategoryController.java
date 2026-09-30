package com.microservice.justanotherapp.controller;

import com.microservice.job.api.dto.CategoryDto;
import com.microservice.justanotherapp.service.CategoryService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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


    // ── PUT /api/categories/{id} ──────────────────────────────────────────

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> update(
            @PathVariable Long id,
            @RequestParam
            @NotBlank(message = "name must not be blank")
            @Size(max = 100, message = "name must not exceed 100 characters")
            String name) {

        log.info("[CategoryController] PUT /api/categories/{}", id);
        return ResponseEntity.ok(categoryService.updateCategory(id, name));
    }

    // ── DELETE /api/categories/{id} ───────────────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("[CategoryController] DELETE /api/categories/{}", id);
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();  // 204
    }
}