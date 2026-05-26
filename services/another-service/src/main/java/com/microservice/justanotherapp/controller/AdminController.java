package com.microservice.justanotherapp.controller;

import com.microservice.justanotherapp.dto.AdminDto;
import com.microservice.justanotherapp.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/admins")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;


    @GetMapping
    public ResponseEntity<List<AdminDto>> getAll() {
        return ResponseEntity.ok(adminService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.findById(id));
    }

    @PostMapping
    public ResponseEntity<AdminDto> create(@RequestBody AdminDto dto) {
        AdminDto created = adminService.create(dto);
        return ResponseEntity.created(URI.create("/api/admins/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminDto> update(@PathVariable Long id, @RequestBody AdminDto dto) {
        return ResponseEntity.ok(adminService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        adminService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

