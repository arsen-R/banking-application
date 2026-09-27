package com.arsen.customerservice.model.dto;

import com.arsen.customerservice.model.entity.IdentityDocument;
import com.arsen.customerservice.model.enums.DocumentType;

import java.time.LocalDate;

public record IdentityDocumentDto(
        String id,
        DocumentType type,
        String maskedDocumentNumber,
        String issuingCountry,
        LocalDate issueDate,
        LocalDate expiryDate
) {
    private static final int VISIBLE_CHARS = 4;

    public static IdentityDocumentDto from(IdentityDocument document) {
        return new IdentityDocumentDto(document.getId(), document.getType(), mask(document.getDocumentNumber()),
                document.getIssuingCountry(), document.getIssueDate(), document.getExpiryDate());
    }

    private static String mask(String value) {
        if (value.length() <= VISIBLE_CHARS) {
            return "*".repeat(value.length());
        }
        return "*".repeat(value.length() - VISIBLE_CHARS) + value.substring(value.length() - VISIBLE_CHARS);
    }
}
