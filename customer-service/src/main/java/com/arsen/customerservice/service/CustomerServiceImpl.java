package com.arsen.customerservice.service;

import com.arsen.customerservice.exception.CustomerNotFoundException;
import com.arsen.customerservice.exception.InvalidCustomerStateException;
import com.arsen.customerservice.messaging.EventTypes;
import com.arsen.customerservice.messaging.Topics;
import com.arsen.customerservice.messaging.outbox.OutboxWriter;
import com.arsen.customerservice.messaging.payload.CustomerCreatedPayload;
import com.arsen.customerservice.messaging.payload.CustomerCreationFailedPayload;
import com.arsen.customerservice.messaging.payload.CustomerKycApprovedPayload;
import com.arsen.customerservice.messaging.payload.CustomerKycRejectedPayload;
import com.arsen.customerservice.messaging.payload.CustomerUpdatedPayload;
import com.arsen.customerservice.messaging.payload.UserRegisteredPayload;
import com.arsen.customerservice.model.dto.CustomerDto;
import com.arsen.customerservice.model.entity.Address;
import com.arsen.customerservice.model.entity.ContactInfo;
import com.arsen.customerservice.model.entity.Customer;
import com.arsen.customerservice.model.entity.IdentityDocument;
import com.arsen.customerservice.model.entity.PersonalInfo;
import com.arsen.customerservice.model.enums.CustomerStatus;
import com.arsen.customerservice.model.enums.KycStatus;
import com.arsen.customerservice.model.request.AddressRequest;
import com.arsen.customerservice.model.request.IdentityDocumentRequest;
import com.arsen.customerservice.model.request.UpdateCustomerRequest;
import com.arsen.customerservice.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {
    static final int MINIMUM_AGE = 18;
    private static final String CUSTOMER_AGGREGATE = "Customer";

    private final CustomerRepository customerRepository;
    private final CustomerNumberGenerator customerNumberGenerator;
    private final OutboxWriter outboxWriter;
    private final Clock clock;

    @Override
    @Transactional
    public void createFromRegistration(UserRegisteredPayload payload) {
        if (customerRepository.existsByAuthUserId(payload.authUserId())) {
            log.info("Customer for auth user {} already exists", payload.authUserId());
            return;
        }
        String rejection = validateRegistration(payload);
        if (rejection != null) {
            log.info("Rejecting customer creation for auth user {}: {}", payload.authUserId(), rejection);
            outboxWriter.write(Topics.CUSTOMER_EVENTS, CUSTOMER_AGGREGATE, payload.authUserId(),
                    EventTypes.CUSTOMER_CREATION_FAILED, new CustomerCreationFailedPayload(payload.authUserId(), rejection));
            return;
        }
        Customer customer = new Customer(
                payload.authUserId(),
                customerNumberGenerator.next(),
                new PersonalInfo(payload.firstName(), payload.middleName(), payload.lastName(), payload.dateOfBirth(), null, null),
                new ContactInfo(payload.phone(), false, payload.email())
        );
        Customer saved = customerRepository.save(customer);
        outboxWriter.write(Topics.CUSTOMER_EVENTS, CUSTOMER_AGGREGATE, saved.getAuthUserId(), EventTypes.CUSTOMER_CREATED,
                new CustomerCreatedPayload(saved.getId(), saved.getAuthUserId(), saved.getCustomerNumber()));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDto getById(String customerId) {
        return CustomerDto.from(findById(customerId));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDto getByAuthUserId(String authUserId) {
        return CustomerDto.from(findByAuthUserId(authUserId));
    }

    @Override
    @Transactional
    public CustomerDto updateByAuthUserId(String authUserId, UpdateCustomerRequest request) {
        Customer customer = findByAuthUserId(authUserId);
        ensureNotClosed(customer);
        PersonalInfo personalInfo = customer.getPersonalInfo();
        personalInfo.setFirstName(request.firstName());
        personalInfo.setMiddleName(request.middleName());
        personalInfo.setLastName(request.lastName());
        personalInfo.setNationality(request.nationality());
        personalInfo.setTaxId(request.taxId());
        ContactInfo contactInfo = customer.getContactInfo();
        if (!request.phone().equals(contactInfo.getPhone())) {
            contactInfo.setPhone(request.phone());
            contactInfo.setPhoneVerified(false);
        }
        contactInfo.setEmail(request.email());

        outboxWriter.write(Topics.CUSTOMER_EVENTS, CUSTOMER_AGGREGATE, customer.getAuthUserId(), EventTypes.CUSTOMER_UPDATED,
                new CustomerUpdatedPayload(customer.getId(), customer.getAuthUserId(), contactInfo.getEmail(), contactInfo.getPhone()));
        return CustomerDto.from(customer);
    }

    @Override
    @Transactional
    public CustomerDto addAddress(String authUserId, AddressRequest request) {
        Customer customer = findByAuthUserId(authUserId);
        ensureNotClosed(customer);
        customer.addAddress(new Address(request.type(), request.line1(), request.line2(), request.city(),
                request.region(), request.postalCode(), request.country()));
        return CustomerDto.from(customerRepository.saveAndFlush(customer));
    }

    @Override
    @Transactional
    public CustomerDto addIdentityDocument(String authUserId, IdentityDocumentRequest request) {
        Customer customer = findByAuthUserId(authUserId);
        ensureNotClosed(customer);
        customer.addDocument(new IdentityDocument(request.type(), request.documentNumber(), request.issuingCountry(),
                request.issueDate(), request.expiryDate(), request.fileRef()));
        return CustomerDto.from(customerRepository.saveAndFlush(customer));
    }

    @Override
    @Transactional
    public CustomerDto approveKyc(String customerId) {
        Customer customer = findById(customerId);
        ensurePendingKyc(customer);
        LocalDate today = LocalDate.now(clock);
        boolean hasValidDocument = customer.getDocuments().stream().anyMatch(d -> d.getExpiryDate().isAfter(today));
        if (!hasValidDocument) {
            throw new InvalidCustomerStateException("Customer has no valid identity document");
        }
        customer.setKycStatus(KycStatus.APPROVED);
        customer.setKycVerifiedAt(Instant.now(clock));
        customer.setKycRejectionReason(null);
        customer.setStatus(CustomerStatus.ACTIVE);

        outboxWriter.write(Topics.CUSTOMER_EVENTS, CUSTOMER_AGGREGATE, customer.getAuthUserId(), EventTypes.CUSTOMER_KYC_APPROVED,
                new CustomerKycApprovedPayload(customer.getId(), customer.getAuthUserId(), customer.getCustomerNumber()));
        return CustomerDto.from(customer);
    }

    @Override
    @Transactional
    public CustomerDto rejectKyc(String customerId, String reason) {
        Customer customer = findById(customerId);
        ensurePendingKyc(customer);
        customer.setKycStatus(KycStatus.REJECTED);
        customer.setKycRejectionReason(reason);
        customer.setStatus(CustomerStatus.REJECTED);

        outboxWriter.write(Topics.CUSTOMER_EVENTS, CUSTOMER_AGGREGATE, customer.getAuthUserId(), EventTypes.CUSTOMER_KYC_REJECTED,
                new CustomerKycRejectedPayload(customer.getId(), customer.getAuthUserId(), reason));
        return CustomerDto.from(customer);
    }

    private String validateRegistration(UserRegisteredPayload payload) {
        if (payload.dateOfBirth() == null) {
            return "Date of birth is required";
        }
        if (Period.between(payload.dateOfBirth(), LocalDate.now(clock)).getYears() < MINIMUM_AGE) {
            return "Customer must be at least " + MINIMUM_AGE + " years old";
        }
        return null;
    }

    private Customer findById(String customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
    }

    private Customer findByAuthUserId(String authUserId) {
        return customerRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
    }

    private void ensurePendingKyc(Customer customer) {
        if (customer.getKycStatus() != KycStatus.PENDING) {
            throw new InvalidCustomerStateException("KYC is already " + customer.getKycStatus());
        }
    }

    private void ensureNotClosed(Customer customer) {
        if (customer.getStatus() == CustomerStatus.CLOSED || customer.getStatus() == CustomerStatus.BLOCKED) {
            throw new InvalidCustomerStateException("Customer is " + customer.getStatus());
        }
    }
}
