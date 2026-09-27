package com.arsen.customerservice.messaging.payload;

public record CustomerUpdatedPayload(String customerId, String authUserId, String email, String phone) {
}
