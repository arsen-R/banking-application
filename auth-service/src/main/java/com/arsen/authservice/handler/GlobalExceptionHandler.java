package com.arsen.authservice.handler;

import com.arsen.authservice.exception.UserAlreadyExistsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Illegal Argument Exception: ", e);
        return ErrorResponse.builder()
                .message(e.getMessage())
                .path(e.getClass().getSimpleName())
                .httpStatus(HttpStatus.CONFLICT)
                .timestamp(Instant.now()).build();
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleUserAlreadyExists(UserAlreadyExistsException e) {
        log.warn("User already exists: {}", e.getMessage());
        return ErrorResponse.builder()
                .message(e.getMessage())
                .path(e.getClass().getSimpleName())
                .httpStatus(HttpStatus.CONFLICT)
                .timestamp(Instant.now()).build();
    }
}
