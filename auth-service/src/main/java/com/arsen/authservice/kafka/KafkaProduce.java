package com.arsen.authservice.kafka;

import com.arsen.authservice.config.KafkaTopicConfig;
import com.arsen.common.model.event.UserCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaProduce {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicConfig kafkaTopicConfig;

    public KafkaProduce(KafkaTemplate<String, Object> kafkaTemplate, KafkaTopicConfig kafkaTopicConfig) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaTopicConfig = kafkaTopicConfig;
    }

    public void sendRegisterUserMessage(UserCreatedEvent event) {
        Message<UserCreatedEvent> message = MessageBuilder.withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, kafkaTopicConfig.userCreatedTopic().name())
                .build();

        kafkaTemplate.send(message) .whenComplete((userCreated, throwable) -> {
            if (throwable == null) {
                log.info("Succeed to sent user event for {} to partition {}", event.getAuthId(), userCreated.getRecordMetadata().partition());
            } else {
                log.error("Failed to send user event for {} : {}", event.getAuthId(), throwable.getMessage());

            }
        });
    }
}
