package com.microservice.justanotherapp.service;

import com.microservice.justanotherapp.dto.AdminDto;
import com.microservice.justanotherapp.dto.UserDto;

import java.util.List;

public interface UserService {
    List<UserDto> findAll();

    UserDto findById(Long id);

    UserDto create(UserDto dto);

    UserDto update(Long id, UserDto dto);

    void delete(Long id);
}

