package com.arsen.authservice.messaging;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@EnableScheduling
public class KafkaConfig {
    @Bean
    public NewTopic authUserEventsTopic() {
        return TopicBuilder.name(Topics.AUTH_USER_EVENTS).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic customerEventsTopic() {
        return TopicBuilder.name(Topics.CUSTOMER_EVENTS).partitions(3).replicas(1).build();
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, String> kafkaTemplate) {
        return new DefaultErrorHandler(new DeadLetterPublishingRecoverer(kafkaTemplate), new FixedBackOff(1000L, 3L));
    }
}
