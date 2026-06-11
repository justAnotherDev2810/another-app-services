package com.microservice.justanotherapp.service.impl;

import com.microservice.justanotherapp.dto.UserDto;
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
        return userRepository.findAll()
                .stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto findById(Long id) {
        User a = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return UserDto.fromEntity(a);
    }

    @Override
    public UserDto create(UserDto dto) {
        try {
            User entity = dto.toEntity();
            // ensure id is null so JPA will generate
            entity.setId(null);
            User saved = userRepository.save(entity);
            return UserDto.fromEntity(saved);
        } catch (DataIntegrityViolationException e) {
            throw new GenericException(
                    "User already exists with username: " + dto.getUsername());
        }
    }

    @Override
    public UserDto update(Long id, UserDto dto) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        existing.setUserName(dto.getUsername());
        existing.setEmail(dto.getEmail());
        existing.setRole(dto.getRole());

        User saved = userRepository.save(existing);
        return UserDto.fromEntity(saved);
    }

    @Override
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        userRepository.deleteById(id);
    }
}
