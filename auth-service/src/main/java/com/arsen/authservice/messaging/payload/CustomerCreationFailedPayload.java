package com.arsen.authservice.messaging.payload;

public record CustomerCreationFailedPayload(String authUserId, String reason) {
}
