package com.bharath.meditrack.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.FieldError;
import org.springframework.validation.BindingResult;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExceptionTest {

    // Simple handler for testing purposes
    @RestControllerAdvice
    static class TestExceptionHandler {

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.builder()
                    .status(404).error("Not Found").message(ex.getMessage())
                    .path(req.getRequestURI()).timestamp(java.time.LocalDateTime.now()).build());
        }

        @ExceptionHandler(BusinessRuleException.class)
        public ResponseEntity<ErrorResponse> handleBusinessRule(BusinessRuleException ex, HttpServletRequest req) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.builder()
                    .status(409).error("Conflict").message(ex.getMessage())
                    .path(req.getRequestURI()).timestamp(java.time.LocalDateTime.now()).build());
        }

        @ExceptionHandler(ValidationException.class)
        public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex, HttpServletRequest req) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.builder()
                    .status(400).error("Bad Request").message(ex.getMessage())
                    .path(req.getRequestURI()).timestamp(java.time.LocalDateTime.now()).build());
        }
    }

    @Test
    void testResourceNotFoundExceptionMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Patient with id 99 not found");
        assertThat(ex.getMessage()).isEqualTo("Patient with id 99 not found");
    }

    @Test
    void testBusinessRuleExceptionMessage() {
        BusinessRuleException ex = new BusinessRuleException("No slots available");
        assertThat(ex.getMessage()).isEqualTo("No slots available");
    }

    @Test
    void testValidationExceptionMessage() {
        ValidationException ex = new ValidationException("Email is invalid");
        assertThat(ex.getMessage()).isEqualTo("Email is invalid");
    }

    @Test
    void testErrorResponseBuilder() {
        ErrorResponse response = ErrorResponse.builder()
                .status(404)
                .error("Not Found")
                .message("Patient with id 99 not found")
                .path("/api/v1/patients/99")
                .timestamp(java.time.LocalDateTime.now())
                .build();

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getError()).isEqualTo("Not Found");
        assertThat(response.getMessage()).isEqualTo("Patient with id 99 not found");
        assertThat(response.getPath()).isEqualTo("/api/v1/patients/99");
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    void testAllExceptionsAreRuntime() {
        assertThat(new ResourceNotFoundException("msg")).isInstanceOf(RuntimeException.class);
        assertThat(new BusinessRuleException("msg")).isInstanceOf(RuntimeException.class);
        assertThat(new ValidationException("msg")).isInstanceOf(RuntimeException.class);
    }
}
