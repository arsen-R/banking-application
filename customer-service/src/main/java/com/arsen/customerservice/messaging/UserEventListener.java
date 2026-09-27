package com.arsen.customerservice.messaging;

import com.arsen.customerservice.messaging.payload.UserRegisteredPayload;
import com.arsen.customerservice.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventListener {
    private final ProcessedEventRepository processedEventRepository;
    private final CustomerService customerService;
    private final JsonMapper jsonMapper;

    @KafkaListener(topics = Topics.AUTH_USER_EVENTS, groupId = "${spring.application.name}")
    @Transactional
    public void onUserEvent(String message) {
        EventEnvelope envelope = jsonMapper.readValue(message, EventEnvelope.class);
        if (processedEventRepository.existsById(envelope.eventId())) {
            log.debug("Skipping already processed event {}", envelope.eventId());
            return;
        }
        if (EventTypes.USER_REGISTERED.equals(envelope.eventType())) {
            customerService.createFromRegistration(jsonMapper.treeToValue(envelope.payload(), UserRegisteredPayload.class));
        } else {
            log.debug("Ignoring event type {}", envelope.eventType());
        }
        processedEventRepository.save(new ProcessedEvent(envelope.eventId()));
    }
}
