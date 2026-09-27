package com.arsen.customerservice.messaging.payload;

public record CustomerCreationFailedPayload(String authUserId, String reason) {
}
