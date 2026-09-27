package com.arsen.authservice.messaging;

import com.arsen.authservice.messaging.payload.CustomerCreatedPayload;
import com.arsen.authservice.messaging.payload.CustomerCreationFailedPayload;
import com.arsen.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerEventListener {
    private final ProcessedEventRepository processedEventRepository;
    private final AuthService authService;
    private final JsonMapper jsonMapper;

    @KafkaListener(topics = Topics.CUSTOMER_EVENTS, groupId = "${spring.application.name}")
    @Transactional
    public void onCustomerEvent(String message) {
        EventEnvelope envelope = jsonMapper.readValue(message, EventEnvelope.class);
        if (processedEventRepository.existsById(envelope.eventId())) {
            log.debug("Skipping already processed event {}", envelope.eventId());
            return;
        }
        switch (envelope.eventType()) {
            case EventTypes.CUSTOMER_CREATED -> {
                CustomerCreatedPayload payload = jsonMapper.treeToValue(envelope.payload(), CustomerCreatedPayload.class);
                authService.activateUser(payload.authUserId());
            }
            case EventTypes.CUSTOMER_CREATION_FAILED -> {
                CustomerCreationFailedPayload payload = jsonMapper.treeToValue(envelope.payload(), CustomerCreationFailedPayload.class);
                authService.disableUser(payload.authUserId(), payload.reason());
            }
            default -> log.debug("Ignoring event type {}", envelope.eventType());
        }
        processedEventRepository.save(new ProcessedEvent(envelope.eventId()));
    }
}
