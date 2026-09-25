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
}