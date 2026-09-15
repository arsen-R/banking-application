package com.arsen.authservice.exception;

public class RegisterUserException extends RuntimeException {
    public RegisterUserException(String message) {
        super(message);
    }
}
