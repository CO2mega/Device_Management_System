package com.dms.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorResponse {
        private int status;
        private String message;
        private LocalDateTime timestamp;
        private Map<String, String> errors;
    }
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ErrorResponse response = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("输入验证失败")
                .timestamp(LocalDateTime.now())
                .errors(errors)
                .build();
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleAuthenticationException(Exception ex) {
        ErrorResponse response = ErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .message("用户名或密码错误")
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}
