package com.arsen.customerservice.messaging.outbox;

import com.arsen.customerservice.messaging.EventEnvelope;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OutboxWriter {
    private static final int EVENT_VERSION = 1;

    private final OutboxEventRepository outboxEventRepository;
    private final JsonMapper jsonMapper;

    @Transactional(propagation = Propagation.MANDATORY)
    public OutboxEvent write(String topic, String aggregateType, String aggregateId, String eventType, Object payload) {
        Instant now = Instant.now();
        String eventId = UUID.randomUUID().toString();
        EventEnvelope envelope = new EventEnvelope(eventId, eventType, EVENT_VERSION, now, aggregateId, jsonMapper.valueToTree(payload));
        OutboxEvent event = new OutboxEvent(eventId, topic, aggregateType, aggregateId, eventType,
                jsonMapper.writeValueAsString(envelope), now, null);
        return outboxEventRepository.save(event);
    }
}
