package com.arsen.customerservice.service;

import com.arsen.customerservice.exception.CustomerNotFoundException;
import com.arsen.customerservice.exception.InvalidCustomerStateException;
import com.arsen.customerservice.messaging.EventTypes;
import com.arsen.customerservice.messaging.Topics;
import com.arsen.customerservice.messaging.outbox.OutboxWriter;
import com.arsen.customerservice.messaging.payload.CustomerCreatedPayload;
import com.arsen.customerservice.messaging.payload.CustomerCreationFailedPayload;
import com.arsen.customerservice.messaging.payload.CustomerKycApprovedPayload;
import com.arsen.customerservice.messaging.payload.UserRegisteredPayload;
import com.arsen.customerservice.model.dto.CustomerDto;
import com.arsen.customerservice.model.entity.ContactInfo;
import com.arsen.customerservice.model.entity.Customer;
import com.arsen.customerservice.model.entity.IdentityDocument;
import com.arsen.customerservice.model.entity.PersonalInfo;
import com.arsen.customerservice.model.enums.CustomerStatus;
import com.arsen.customerservice.model.enums.DocumentType;
import com.arsen.customerservice.model.enums.KycStatus;
import com.arsen.customerservice.model.request.UpdateCustomerRequest;
import com.arsen.customerservice.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-01-15T10:00:00Z"), ZoneOffset.UTC);

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private CustomerNumberGenerator customerNumberGenerator;
    @Mock
    private OutboxWriter outboxWriter;

    private CustomerServiceImpl customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerServiceImpl(customerRepository, customerNumberGenerator, outboxWriter, CLOCK);
    }

    @Test
    void createFromRegistrationShouldSaveCustomerAndEmitCustomerCreated() {
        UserRegisteredPayload payload = registration("auth-1", LocalDate.of(1990, 5, 20));
        when(customerRepository.existsByAuthUserId("auth-1")).thenReturn(false);
        when(customerNumberGenerator.next()).thenReturn("CUS-0000000001");
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> {
            Customer c = inv.getArgument(0);
            c.setId("cust-1");
            return c;
        });

        customerService.createFromRegistration(payload);

        ArgumentCaptor<Customer> saved = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(saved.capture());
        assertThat(saved.getValue().getAuthUserId()).isEqualTo("auth-1");
        assertThat(saved.getValue().getStatus()).isEqualTo(CustomerStatus.PENDING_KYC);
        assertThat(saved.getValue().getKycStatus()).isEqualTo(KycStatus.PENDING);
        assertThat(saved.getValue().getPersonalInfo().getFirstName()).isEqualTo("Emily");
        assertThat(saved.getValue().getContactInfo().getEmail()).isEqualTo("emily@example.com");
        verify(outboxWriter).write(Topics.CUSTOMER_EVENTS, "Customer", "auth-1", EventTypes.CUSTOMER_CREATED,
                new CustomerCreatedPayload("cust-1", "auth-1", "CUS-0000000001"));
    }

    @Test
    void createFromRegistrationShouldEmitFailureWhenCustomerIsUnderage() {
        UserRegisteredPayload payload = registration("auth-2", LocalDate.of(2010, 1, 1));
        when(customerRepository.existsByAuthUserId("auth-2")).thenReturn(false);

        customerService.createFromRegistration(payload);

        verify(customerRepository, never()).save(any());
        verify(outboxWriter).write(eq(Topics.CUSTOMER_EVENTS), eq("Customer"), eq("auth-2"),
                eq(EventTypes.CUSTOMER_CREATION_FAILED), any(CustomerCreationFailedPayload.class));
    }

    @Test
    void createFromRegistrationShouldDoNothingWhenCustomerAlreadyExists() {
        when(customerRepository.existsByAuthUserId("auth-3")).thenReturn(true);

        customerService.createFromRegistration(registration("auth-3", LocalDate.of(1990, 1, 1)));

        verify(customerRepository, never()).save(any());
        verifyNoInteractions(outboxWriter, customerNumberGenerator);
    }

    @Test
    void approveKycShouldActivateCustomerWhenValidDocumentExists() {
        Customer customer = customer("cust-4", "auth-4");
        customer.addDocument(new IdentityDocument(DocumentType.PASSPORT, "AB1234567", "US",
                LocalDate.of(2020, 1, 1), LocalDate.of(2030, 1, 1), null));
        when(customerRepository.findById("cust-4")).thenReturn(Optional.of(customer));

        CustomerDto result = customerService.approveKyc("cust-4");

        assertThat(result.status()).isEqualTo(CustomerStatus.ACTIVE);
        assertThat(result.kycStatus()).isEqualTo(KycStatus.APPROVED);
        assertThat(result.kycVerifiedAt()).isEqualTo(Instant.now(CLOCK));
        assertThat(result.documents().getFirst().maskedDocumentNumber()).isEqualTo("*****4567");
        verify(outboxWriter).write(Topics.CUSTOMER_EVENTS, "Customer", "auth-4", EventTypes.CUSTOMER_KYC_APPROVED,
                new CustomerKycApprovedPayload("cust-4", "auth-4", "CUS-0000000004"));
    }

    @Test
    void approveKycShouldFailWhenNoValidDocument() {
        Customer customer = customer("cust-5", "auth-5");
        customer.addDocument(new IdentityDocument(DocumentType.PASSPORT, "AB1234567", "US",
                LocalDate.of(2015, 1, 1), LocalDate.of(2025, 1, 1), null));
        when(customerRepository.findById("cust-5")).thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> customerService.approveKyc("cust-5")).isInstanceOf(InvalidCustomerStateException.class);
        verifyNoInteractions(outboxWriter);
    }

    @Test
    void rejectKycShouldFailWhenKycAlreadyDecided() {
        Customer customer = customer("cust-6", "auth-6");
        customer.setKycStatus(KycStatus.APPROVED);
        when(customerRepository.findById("cust-6")).thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> customerService.rejectKyc("cust-6", "fraud")).isInstanceOf(InvalidCustomerStateException.class);
        verifyNoInteractions(outboxWriter);
    }

    @Test
    void updateShouldResetPhoneVerificationWhenPhoneChanges() {
        Customer customer = customer("cust-7", "auth-7");
        customer.getContactInfo().setPhoneVerified(true);
        when(customerRepository.findByAuthUserId("auth-7")).thenReturn(Optional.of(customer));

        CustomerDto result = customerService.updateByAuthUserId("auth-7", new UpdateCustomerRequest(
                "Emily", null, "Ramirez", "US", null, "new@example.com", "+12025550199"));

        assertThat(result.phone()).isEqualTo("+12025550199");
        assertThat(result.phoneVerified()).isFalse();
        assertThat(result.email()).isEqualTo("new@example.com");
        verify(outboxWriter).write(eq(Topics.CUSTOMER_EVENTS), eq("Customer"), eq("auth-7"), eq(EventTypes.CUSTOMER_UPDATED), any());
    }

    @Test
    void getByAuthUserIdShouldThrowWhenCustomerMissing() {
        when(customerRepository.findByAuthUserId(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.getByAuthUserId("missing")).isInstanceOf(CustomerNotFoundException.class);
    }

    private static UserRegisteredPayload registration(String authUserId, LocalDate dateOfBirth) {
        return new UserRegisteredPayload(authUserId, "emily", "emily@example.com", "Emily", null, "Ramirez",
                dateOfBirth, "+12025550124");
    }

    private static Customer customer(String id, String authUserId) {
        Customer customer = new Customer(authUserId, "CUS-000000000" + id.substring(id.length() - 1),
                new PersonalInfo("Emily", null, "Ramirez", LocalDate.of(1990, 5, 20), null, null),
                new ContactInfo("+12025550124", false, "emily@example.com"));
        customer.setId(id);
        return customer;
    }
}
