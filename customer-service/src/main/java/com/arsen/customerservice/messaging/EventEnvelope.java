package com.arsen.customerservice.messaging;

import tools.jackson.databind.JsonNode;

import java.time.Instant;

public record EventEnvelope(
        String eventId,
        String eventType,
        int version,
        Instant occurredAt,
        String aggregateId,
        JsonNode payload
) {
}
