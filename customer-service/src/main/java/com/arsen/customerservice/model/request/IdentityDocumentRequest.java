package com.arsen.customerservice.model.request;

import com.arsen.customerservice.model.enums.DocumentType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record IdentityDocumentRequest(
        @NotNull(message = "The document type is required")
        DocumentType type,
        @NotBlank(message = "The document number is required")
        String documentNumber,
        @NotBlank(message = "The issuing country is required")
        @Size(min = 2, max = 2, message = "Issuing country must be an ISO 3166-1 alpha-2 code")
        String issuingCountry,
        @PastOrPresent(message = "The issue date cannot be in the future")
        LocalDate issueDate,
        @NotNull(message = "The expiry date is required")
        @Future(message = "The document is expired")
        LocalDate expiryDate,
        String fileRef
) {
}
