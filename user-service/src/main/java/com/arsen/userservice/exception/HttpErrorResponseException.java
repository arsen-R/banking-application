package com.arsen.userservice.exception;

import org.springframework.http.HttpStatus;

public class HttpErrorResponseException extends RuntimeException {
    private final String message;
    private final HttpStatus httpStatus;

    public HttpErrorResponseException(String message, HttpStatus httpStatus) {
        super(message);
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
