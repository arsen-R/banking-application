package com.arsen.authservice.messaging;

import com.arsen.authservice.messaging.payload.CustomerCreatedPayload;
import com.arsen.authservice.messaging.payload.CustomerCreationFailedPayload;
import com.arsen.authservice.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerEventListenerTest {
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Mock
    private ProcessedEventRepository processedEventRepository;
    @Mock
    private AuthService authService;

    private CustomerEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new CustomerEventListener(processedEventRepository, authService, jsonMapper);
    }

    @Test
    void customerCreatedShouldActivateUser() {
        when(processedEventRepository.existsById("evt-1")).thenReturn(false);

        listener.onCustomerEvent(envelope("evt-1", EventTypes.CUSTOMER_CREATED,
                new CustomerCreatedPayload("cust-1", "user-1", "CUS-0000000001")));

        verify(authService).activateUser("user-1");
        verify(processedEventRepository).save(any(ProcessedEvent.class));
    }

    @Test
    void customerCreationFailedShouldDisableUser() {
        when(processedEventRepository.existsById("evt-2")).thenReturn(false);

        listener.onCustomerEvent(envelope("evt-2", EventTypes.CUSTOMER_CREATION_FAILED,
                new CustomerCreationFailedPayload("user-1", "underage")));

        verify(authService).disableUser("user-1", "underage");
    }

    @Test
    void duplicateEventShouldBeIgnored() {
        when(processedEventRepository.existsById("evt-3")).thenReturn(true);

        listener.onCustomerEvent(envelope("evt-3", EventTypes.CUSTOMER_CREATED,
                new CustomerCreatedPayload("cust-1", "user-1", "CUS-0000000001")));

        verifyNoInteractions(authService);
        verify(processedEventRepository, never()).save(any());
    }

    private String envelope(String eventId, String type, Object payload) {
        return jsonMapper.writeValueAsString(new EventEnvelope(eventId, type, 1, Instant.now(), "user-1",
                jsonMapper.valueToTree(payload)));
    }
}
