package com.microservice.job.common.exceptionHandler;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standard error response shape returned by the GlobalExceptionHandler.
 *
 * Every error from every service looks identical — makes frontend
 * error handling simple and predictable.
 *
 * Shape:
 * {
 *   "timestamp": "2026-08-15T10:30:00",
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "User not found with id: 99",
 *   "path": "/api/user/99",
 *   "errors": []   // populated for validation errors
 * }
 **/
@Data
@Builder
public class ApiErrorResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private int status;
    private String error;
    private String message;
    private String path;

    // Populated for validation errors — lists each field-level problem
    private List<String> errors;
}