package com.arsen.customerservice.messaging.payload;

public record CustomerCreatedPayload(String customerId, String authUserId, String customerNumber) {
}
