package com.arsen.customerservice.handler;

import lombok.Builder;

import java.time.Instant;
import java.util.Map;

@Builder
public record ErrorResponse(
        String error,
        String message,
        Integer status,
        Map<String, String> details,
        Instant timestamp
) {
}
