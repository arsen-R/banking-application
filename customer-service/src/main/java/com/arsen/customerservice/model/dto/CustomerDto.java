package com.arsen.customerservice.model.dto;

import com.arsen.customerservice.model.entity.Customer;
import com.arsen.customerservice.model.enums.CustomerStatus;
import com.arsen.customerservice.model.enums.CustomerType;
import com.arsen.customerservice.model.enums.KycStatus;
import com.arsen.customerservice.model.enums.RiskLevel;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record CustomerDto(
        String id,
        String authUserId,
        String customerNumber,
        CustomerType type,
        CustomerStatus status,
        String firstName,
        String middleName,
        String lastName,
        LocalDate dateOfBirth,
        String nationality,
        String email,
        String phone,
        boolean phoneVerified,
        KycStatus kycStatus,
        Instant kycVerifiedAt,
        RiskLevel riskLevel,
        List<AddressDto> addresses,
        List<IdentityDocumentDto> documents
) {
    public static CustomerDto from(Customer customer) {
        return new CustomerDto(
                customer.getId(),
                customer.getAuthUserId(),
                customer.getCustomerNumber(),
                customer.getType(),
                customer.getStatus(),
                customer.getPersonalInfo().getFirstName(),
                customer.getPersonalInfo().getMiddleName(),
                customer.getPersonalInfo().getLastName(),
                customer.getPersonalInfo().getDateOfBirth(),
                customer.getPersonalInfo().getNationality(),
                customer.getContactInfo().getEmail(),
                customer.getContactInfo().getPhone(),
                customer.getContactInfo().isPhoneVerified(),
                customer.getKycStatus(),
                customer.getKycVerifiedAt(),
                customer.getRiskLevel(),
                customer.getAddresses().stream().map(AddressDto::from).toList(),
                customer.getDocuments().stream().map(IdentityDocumentDto::from).toList()
        );
    }
}
