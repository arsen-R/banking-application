package com.arsen.authservice.messaging.payload;

public record CustomerCreatedPayload(String customerId, String authUserId, String customerNumber) {
}
