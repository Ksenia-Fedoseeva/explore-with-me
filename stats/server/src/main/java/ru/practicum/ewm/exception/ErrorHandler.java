package ru.practicum.ewm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

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
}