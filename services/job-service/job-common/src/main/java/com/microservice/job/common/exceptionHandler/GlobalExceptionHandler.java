package com.microservice.job.common.exceptionHandler;

import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Global exception handler — catches exceptions thrown anywhere in any
 * service that imports job-common, and returns a consistent ApiErrorResponse.
 *
 * Without this, Spring returns its default whitepage error or a raw stack trace.
 * With this, every error looks the same and is loggable + debuggable.
 *
 * To activate in a service, just import job-common as a dependency —
 * Spring's component scan picks up @RestControllerAdvice automatically
 * as long as the base package is scanned.
 *
 * Add to your service's @SpringBootApplication:
 *   \/*@SpringBootApplication(scanBasePackages = {
 *       "com.microservice.justanotherapp",
 *       "com.microservice.job.common"         ← this line
 *   })*\/
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 404 Not Found ──────────────────────────────────────────────────────
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {

        log.warn("[GlobalExceptionHandler] 404 Not Found: {} | path: {}",
                ex.getMessage(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildError(HttpStatus.NOT_FOUND, ex.getMessage(),
                        request.getRequestURI(), null));
    }

    // ── 409 Conflict ───────────────────────────────────────────────────────
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicate(
            DuplicateResourceException ex, HttpServletRequest request) {

        log.warn("[GlobalExceptionHandler] 409 Conflict: {} | path: {}",
                ex.getMessage(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildError(HttpStatus.CONFLICT, ex.getMessage(),
                        request.getRequestURI(), null));
    }

    // ── 409 from DB unique constraint violation ────────────────────────────
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException ex, HttpServletRequest request) {

        String message = "A record with the same unique value already exists";
        log.warn("[GlobalExceptionHandler] 409 DataIntegrity: {} | path: {}",
                ex.getMostSpecificCause().getMessage(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildError(HttpStatus.CONFLICT, message,
                        request.getRequestURI(), null));
    }

    // ── 400 Validation errors (@Valid on request body) ────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();

        log.warn("[GlobalExceptionHandler] 400 Validation failed: {} | path: {}",
                errors, request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildError(HttpStatus.BAD_REQUEST,
                        "Validation failed", request.getRequestURI(), errors));
    }

    // ── 500 Catch-all ──────────────────────────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest request) {

        log.error("[GlobalExceptionHandler] 500 Unexpected error: {} | path: {}",
                ex.getMessage(), request.getRequestURI(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildError(HttpStatus.INTERNAL_SERVER_ERROR,
                        "An unexpected error occurred",
                        request.getRequestURI(), null));
    }

    // ── Builder helper ─────────────────────────────────────────────────────
    private ApiErrorResponse buildError(HttpStatus status, String message,
                                        String path, List<String> errors) {
        return ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .errors(errors)
                .build();
    }
}