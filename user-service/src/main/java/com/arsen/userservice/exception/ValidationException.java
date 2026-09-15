package com.arsen.userservice.exception;

import java.util.Map;

public class ValidationException extends RuntimeException {
    private Map<String, String> validationErrors;

    public ValidationException(Map<String, String> validationErrors) {
        this.validationErrors = validationErrors;
    }
    public ValidationException(String message) {
        super(message);
    }
}
