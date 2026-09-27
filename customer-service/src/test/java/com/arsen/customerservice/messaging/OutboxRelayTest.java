package com.arsen.customerservice.messaging;

import com.arsen.customerservice.messaging.outbox.OutboxEvent;
import com.arsen.customerservice.messaging.outbox.OutboxEventRepository;
import com.arsen.customerservice.messaging.outbox.OutboxRelay;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxRelayTest {
    @Mock
    private OutboxEventRepository outboxEventRepository;
    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;
    @InjectMocks
    private OutboxRelay outboxRelay;

    @Test
    void shouldPublishInOrderAndStopAtFirstFailure() {
        OutboxEvent first = event("e1");
        OutboxEvent second = event("e2");
        OutboxEvent third = event("e3");
        when(outboxEventRepository.findTop100ByPublishedAtIsNullOrderByCreatedAtAsc()).thenReturn(List.of(first, second, third));
        @SuppressWarnings("unchecked")
        SendResult<String, String> result = mock(SendResult.class);
        when(kafkaTemplate.send(Topics.CUSTOMER_EVENTS, "agg", "e1")).thenReturn(CompletableFuture.completedFuture(result));
        when(kafkaTemplate.send(Topics.CUSTOMER_EVENTS, "agg", "e2")).thenReturn(CompletableFuture.failedFuture(new RuntimeException("broker down")));

        outboxRelay.publishPending();

        assertThat(first.getPublishedAt()).isNotNull();
        assertThat(second.getPublishedAt()).isNull();
        assertThat(third.getPublishedAt()).isNull();
        verify(kafkaTemplate, never()).send(Topics.CUSTOMER_EVENTS, "agg", "e3");
    }

    private static OutboxEvent event(String id) {
        return new OutboxEvent(id, Topics.CUSTOMER_EVENTS, "Customer", "agg", "CustomerCreated", id, Instant.now(), null);
    }
}
