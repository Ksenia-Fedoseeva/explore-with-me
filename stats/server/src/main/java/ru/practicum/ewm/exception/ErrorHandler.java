package ru.practicum.ewm.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAll(Exception ex) {
        return new ResponseEntity<>(
                Map.of(
                        "status", HttpStatus.INTERNAL_SERVER_ERROR,
                        "reason", "Unexpected error",
                        "message", ex.getMessage(),
                        "timestamp", LocalDateTime.now()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return new ResponseEntity<>(
                Map.of(
                        "status", HttpStatus.BAD_REQUEST,
                        "reason", "Validation error",
                        "message", message,
                        "timestamp", LocalDateTime.now()
                ),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));

        return new ResponseEntity<>(
                Map.of(
                        "status", HttpStatus.BAD_REQUEST,
                        "reason", "Validation error",
                        "message", message,
                        "timestamp", LocalDateTime.now()
                ),
                HttpStatus.BAD_REQUEST
        );
    }
}