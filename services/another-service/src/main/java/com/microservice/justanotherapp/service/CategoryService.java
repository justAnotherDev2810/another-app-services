package com.microservice.justanotherapp.service;

import com.microservice.job.api.dto.CategoryDto;

import java.util.List;

public interface CategoryService {
    List<CategoryDto> getAllCategories();
}