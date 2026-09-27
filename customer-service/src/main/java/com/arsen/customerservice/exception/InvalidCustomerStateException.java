package com.arsen.customerservice.exception;

public class InvalidCustomerStateException extends RuntimeException {
    public InvalidCustomerStateException(String message) {
        super(message);
    }
}
