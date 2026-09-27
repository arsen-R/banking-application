package com.arsen.customerservice.service;

import com.arsen.customerservice.messaging.payload.UserRegisteredPayload;
import com.arsen.customerservice.model.dto.CustomerDto;
import com.arsen.customerservice.model.request.AddressRequest;
import com.arsen.customerservice.model.request.IdentityDocumentRequest;
import com.arsen.customerservice.model.request.UpdateCustomerRequest;

public interface CustomerService {
    void createFromRegistration(UserRegisteredPayload payload);

    CustomerDto getById(String customerId);

    CustomerDto getByAuthUserId(String authUserId);

    CustomerDto updateByAuthUserId(String authUserId, UpdateCustomerRequest request);

    CustomerDto addAddress(String authUserId, AddressRequest request);

    CustomerDto addIdentityDocument(String authUserId, IdentityDocumentRequest request);

    CustomerDto approveKyc(String customerId);

    CustomerDto rejectKyc(String customerId, String reason);
}
