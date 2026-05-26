package com.microservice.justanotherapp.service.impl;

import com.microservice.justanotherapp.dto.AdminDto;
import com.microservice.justanotherapp.entity.Admin;
import com.microservice.justanotherapp.repository.AdminRepository;
import com.microservice.justanotherapp.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;


    @Override
    public List<AdminDto> findAll() {
        return adminRepository.findAll()
                .stream()
                .map(AdminDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public AdminDto findById(Long id) {
        Admin a = adminRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found"));
        return AdminDto.fromEntity(a);
    }

    @Override
    public AdminDto create(AdminDto dto) {
        Admin entity = dto.toEntity();
        // ensure id is null so JPA will generate
        entity.setId(null);
        Admin saved = adminRepository.save(entity);
        return AdminDto.fromEntity(saved);
    }

    @Override
    public AdminDto update(Long id, AdminDto dto) {
        Admin existing = adminRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found"));

        existing.setUsername(dto.getUsername());
        existing.setEmail(dto.getEmail());
        existing.setRole(dto.getRole());

        Admin saved = adminRepository.save(existing);
        return AdminDto.fromEntity(saved);
    }

    @Override
    public void delete(Long id) {
        if (!adminRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found");
        }
        adminRepository.deleteById(id);
    }
}

