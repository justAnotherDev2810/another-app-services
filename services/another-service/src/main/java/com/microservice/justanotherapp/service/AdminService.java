package com.microservice.justanotherapp.service;

import com.microservice.justanotherapp.dto.AdminDto;

import java.util.List;

public interface AdminService {
    List<AdminDto> findAll();

    AdminDto findById(Long id);

    AdminDto create(AdminDto dto);

    AdminDto update(Long id, AdminDto dto);

    void delete(Long id);
}

