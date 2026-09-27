package com.arsen.customerservice.messaging.payload;

public record CustomerKycApprovedPayload(String customerId, String authUserId, String customerNumber) {
}
