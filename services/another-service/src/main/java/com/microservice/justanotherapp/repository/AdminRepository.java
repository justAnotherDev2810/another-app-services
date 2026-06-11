package com.microservice.justanotherapp.repository;

import com.microservice.justanotherapp.entity.Admin;

import java.util.List;
import java.util.Optional;

import com.microservice.justanotherapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Simple repository abstraction for Admins. Implemented as an in-memory store in AdminRepositoryImpl.
 */

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long>  {
    List<Admin> findAll();

    Optional<Admin> findById(Long id);

    Admin save(Admin admin);

    void deleteById(Long id);

    boolean existsById(Long id);
}

