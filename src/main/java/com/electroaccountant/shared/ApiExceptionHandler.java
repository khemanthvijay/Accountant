package com.electroaccountant.shared;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ApiException.class)
    ResponseEntity<Map<String, Object>> handleApiException(ApiException exception) {
        return response(exception.status(), exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst().map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("Invalid request");
        return response(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(DataAccessException.class)
    ResponseEntity<Map<String, Object>> handleDatabaseFailure(DataAccessException exception) {
        Throwable cause = exception.getMostSpecificCause();
        String detail = cause.getMessage() == null ? "Unknown database error" : cause.getMessage();
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "Database error: " + detail);
    }

    private ResponseEntity<Map<String, Object>> response(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "message", message));
    }
}
