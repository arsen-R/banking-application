package com.arsen.customerservice.handler;

import com.arsen.customerservice.exception.CustomerNotFoundException;
import com.arsen.customerservice.exception.InvalidCustomerStateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> details = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> details.put(error.getField(), error.getDefaultMessage()));
        e.getBindingResult().getGlobalErrors().forEach(error -> details.put(error.getObjectName(), error.getDefaultMessage()));
        return build(HttpStatus.BAD_REQUEST, e.getClass().getSimpleName(), "Validation failed", details);
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(CustomerNotFoundException e) {
        log.warn("Customer not found: {}", e.getMessage());
        return build(HttpStatus.NOT_FOUND, e.getClass().getSimpleName(), e.getMessage(), null);
    }

    @ExceptionHandler(InvalidCustomerStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleInvalidState(InvalidCustomerStateException e) {
        log.warn("Invalid customer state: {}", e.getMessage());
        return build(HttpStatus.CONFLICT, e.getClass().getSimpleName(), e.getMessage(), null);
    }

    private ErrorResponse build(HttpStatus status, String error, String message, Map<String, String> details) {
        return ErrorResponse.builder()
                .error(error)
                .message(message)
                .status(status.value())
                .details(details)
                .timestamp(Instant.now())
                .build();
    }
}
