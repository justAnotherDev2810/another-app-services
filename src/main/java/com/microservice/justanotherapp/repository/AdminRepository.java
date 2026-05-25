package com.microservice.justanotherapp.repository;

import com.microservice.justanotherapp.entity.Admin;

import java.util.List;
import java.util.Optional;

/**
 * Simple repository abstraction for Admins. Implemented as an in-memory store in AdminRepositoryImpl.
 */
public interface AdminRepository {
    List<Admin> findAll();

    Optional<Admin> findById(Long id);

    Admin save(Admin admin);

    void deleteById(Long id);

    boolean existsById(Long id);
}

