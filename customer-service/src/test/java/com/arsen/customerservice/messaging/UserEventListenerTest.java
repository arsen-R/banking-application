package com.arsen.customerservice.messaging;

import com.arsen.customerservice.messaging.payload.UserRegisteredPayload;
import com.arsen.customerservice.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserEventListenerTest {
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Mock
    private ProcessedEventRepository processedEventRepository;
    @Mock
    private CustomerService customerService;

    private UserEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new UserEventListener(processedEventRepository, customerService, jsonMapper);
    }

    @Test
    void shouldCreateCustomerAndMarkEventProcessed() {
        UserRegisteredPayload payload = new UserRegisteredPayload("auth-1", "emily", "emily@example.com",
                "Emily", null, "Ramirez", LocalDate.of(1990, 5, 20), "+12025550124");
        when(processedEventRepository.existsById("evt-1")).thenReturn(false);

        listener.onUserEvent(envelope("evt-1", EventTypes.USER_REGISTERED, payload));

        verify(customerService).createFromRegistration(payload);
        ArgumentCaptor<ProcessedEvent> processed = ArgumentCaptor.forClass(ProcessedEvent.class);
        verify(processedEventRepository).save(processed.capture());
        assertThat(processed.getValue().getEventId()).isEqualTo("evt-1");
    }

    @Test
    void shouldSkipDuplicateEvent() {
        when(processedEventRepository.existsById("evt-2")).thenReturn(true);

        listener.onUserEvent(envelope("evt-2", EventTypes.USER_REGISTERED, null));

        verifyNoInteractions(customerService);
        verify(processedEventRepository, never()).save(any());
    }

    private String envelope(String eventId, String type, Object payload) {
        return jsonMapper.writeValueAsString(new EventEnvelope(eventId, type, 1, Instant.now(), "auth-1",
                jsonMapper.valueToTree(payload)));
    }
}
