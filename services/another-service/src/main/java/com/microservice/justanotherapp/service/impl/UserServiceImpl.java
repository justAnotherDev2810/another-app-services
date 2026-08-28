package com.microservice.justanotherapp.service.impl;

import com.microservice.job.api.dto.UserDto;
import com.microservice.job.common.utils.LogUtils;
import com.microservice.justanotherapp.entity.User;
import com.microservice.justanotherapp.exception.GenericException;
import com.microservice.justanotherapp.repository.UserRepository;
import com.microservice.justanotherapp.service.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<UserDto> findAll() {
        LogUtils.startLog("UserServiceImpl", "findAll");
        List<UserDto> users = userRepository.findAll()
                .stream()
                .map(User::fromEntity)
                .collect(Collectors.toList());
        LogUtils.logInfoMessage("Found " + users.size() + " users");
        LogUtils.endLog("UserServiceImpl", "findAll");
        return users;
    }

    @Override
    public UserDto findById(Long id) {
        LogUtils.startLog("UserServiceImpl", "findById");
        User a = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return User.fromEntity(a);
    }

    @Override
    public UserDto create(UserDto dto) {
        LogUtils.startLog("UserServiceImpl", "create");
        try {
            User entity = User.toEntity(dto);
            LogUtils.logInfoMessage("Creating user with username: " + entity.getUserName());
            // ensure id is null so JPA will generate
            entity.setId(null);
            User saved = userRepository.save(entity);
            LogUtils.logInfoMessage("User created with ID: " + saved.getId());
            LogUtils.endLog("UserServiceImpl", "create");
            return User.fromEntity(saved);
        } catch (DataIntegrityViolationException e) {
            LogUtils.errorMessage("UserServiceImpl", "create", "DataIntegrityViolationException: " + e.getMessage());
            LogUtils.endLog("UserServiceImpl", "create");
            throw new GenericException(
                    "User already exists with username: " + dto.getUsername());
        }
    }

    @Override
    public UserDto update(Long id, UserDto dto) {
        LogUtils.startLog("UserServiceImpl", "update");
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        existing.setUserName(dto.getUsername());
        existing.setEmail(dto.getEmail());
        existing.setRole(dto.getRole());

        User saved = userRepository.save(existing);
        LogUtils.logInfoMessage("User updated with ID: " + saved.getId());
        LogUtils.endLog("UserServiceImpl", "update");
        return User.fromEntity(saved);
    }

    @Override
    public void delete(Long id) {
        LogUtils.startLog("UserServiceImpl", "delete");
        if (!userRepository.existsById(id)) {
            LogUtils.errorMessage("UserServiceImpl", "delete", "User not found with ID: " + id);
            LogUtils.endLog("UserServiceImpl", "delete");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        userRepository.deleteById(id);
        LogUtils.endLog("UserServiceImpl", "delete");
    }
}
