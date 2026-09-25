package com.microservice.justanotherapp.service.impl;

import com.microservice.job.api.dto.CategoryDto;
import com.microservice.job.common.utils.LogUtils;
import com.microservice.justanotherapp.repository.CategoryRepository;
import com.microservice.justanotherapp.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
}