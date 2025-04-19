package ru.practicum.ewm.exception;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException e) {
        log.error("NotFoundException: {}", e.getMessage());
        return buildResponse(
                List.of(),
                e.getMessage(),
                "Объект не найден",
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException e) {
        log.error("ConflictException: {}", e.getMessage());
        return buildResponse(
                List.of(),
                e.getMessage(),
                "Конфликт данных",
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException e) {
        List<String> errorMessages = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        log.error("ValidationException: {}", errorMessages);
        return buildResponse(
                errorMessages,
                "Ошибка валидации полей",
                "Некорректные параметры запроса",
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParam(MissingServletRequestParameterException e) {
        String message = "Отсутствует обязательный параметр запроса: " + e.getParameterName();
        log.error("MissingServletRequestParameterException: {}", message);
        return buildResponse(
                List.of(message),
                message,
                "Ошибка параметров запроса",
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiError> handleValidation(ValidationException e) {
        log.error("ValidationException: {}", e.getMessage());
        return buildResponse(
                List.of(),
                e.getMessage(),
                "Ошибка валидации",
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.error("DataIntegrityViolationException: {}", e.getMessage());
        return buildResponse(
                List.of(e.getRootCause() != null ? e.getRootCause().getMessage() : e.getMessage()),
                "Пользователь с таким email уже существует",
                "Нарушение уникальности или других ограничений БД",
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleException(Exception e) {
        log.error("InternalServerError: {}", e.getMessage(), e);
        return buildResponse(
                List.of(e.getMessage()),
                "Внутренняя ошибка сервера",
                "Произошла непредвиденная ошибка",
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    private ResponseEntity<ApiError> buildResponse(List<String> errors, String message, String reason, HttpStatus status) {
        return new ResponseEntity<>(
                ApiError.builder()
                        .errors(errors)
                        .message(message)
                        .reason(reason)
                        .status(status.name())
                        .timestamp(LocalDateTime.now())
                        .build(),
                status
        );
    }
}
