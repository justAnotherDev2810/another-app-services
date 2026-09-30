package com.microservice.justanotherapp.service.impl;

import com.microservice.job.api.dto.CategoryDto;
import com.microservice.job.common.exception.DuplicateResourceException;
import com.microservice.job.common.exception.ResourceNotFoundException;
import com.microservice.job.common.utils.LogUtils;
import com.microservice.justanotherapp.entity.Category;
import com.microservice.justanotherapp.repository.CategoryRepository;
import com.microservice.justanotherapp.service.CategoryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryDto> getAllCategories() {
        LogUtils.startLog("CategoryServiceImpl", "findAllCategories");
        LogUtils.logInfoMessage("[CategoryService] Fetching all categories");
        List<CategoryDto> list = categoryRepository.findAllCategories();
        LogUtils.logInfoMessage("[CategoryService] Found "+ list.size()+" categories");
        LogUtils.endLog("CategoryServiceImpl", "findAllCategories");
        return list;
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long id, String name) {
        log.info("[CategoryService] Updating category id={} name={}", id, name);

        // 404 if category doesn't exist
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        // Check for name collision BEFORE hitting the DB unique constraint.
        // excludes the current record so renaming to the same name is a no-op, not an error.
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateResourceException("Category", "name", name);
        }

        existing.setName(name);
        Category saved = categoryRepository.save(existing);

        log.info("[CategoryService] Updated category id={}", saved.getId());
        return CategoryDto.builder()
                .id(saved.getId())
                .name(saved.getName())
                .build();
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        log.info("[CategoryService] Deleting category id={}", id);

        // 404 if category doesn't exist
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category", "id", id);
        }

        try {
            categoryRepository.deleteById(id);
            // Flush forces the DELETE to execute within the transaction so we
            // can catch the FK constraint violation here rather than at commit time.
            categoryRepository.flush();
            log.info("[CategoryService] Deleted category id={}", id);

        } catch (DataIntegrityViolationException e) {
            // ON DELETE RESTRICT fires when the category still has expenses.
            // Catch here and return a clean 409 — no raw SQL leaks to the client.
            log.warn("[CategoryService] Cannot delete category id={} — has expenses attached", id);
            throw new DuplicateResourceException(
                    "Category has expenses attached and cannot be deleted");
        }
    }
}