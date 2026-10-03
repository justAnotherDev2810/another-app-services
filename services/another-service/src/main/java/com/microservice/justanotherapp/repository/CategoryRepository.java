package com.microservice.justanotherapp.repository;

import com.microservice.job.api.dto.CategoryDto;
import com.microservice.justanotherapp.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT new com.microservice.job.api.dto.CategoryDto(c.id, c.name) FROM Category c")
    List<CategoryDto> findAllCategories();

    /**
     * Used on CREATE — checks if any category with this name already exists.
     * Case-insensitive.
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Checks whether a category with the given name already exists,
     * excluding the category with the given id.
     *
     * Used on PUT to catch duplicate name collisions before hitting
     * the DB unique constraint — returns a clean 409 instead of a
     * raw DataIntegrityViolationException.
     */
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}