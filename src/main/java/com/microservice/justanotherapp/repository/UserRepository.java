package com.microservice.justanotherapp.repository;

import com.microservice.justanotherapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // custom queries can be added here
}
