package com.arsen.customerservice.messaging.payload;

public record CustomerKycRejectedPayload(String customerId, String authUserId, String reason) {
}
