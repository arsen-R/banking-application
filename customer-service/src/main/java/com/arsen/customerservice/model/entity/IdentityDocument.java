package com.arsen.customerservice.model.entity;

import com.arsen.customerservice.model.enums.DocumentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "customer_identity_documents")
@Getter
@Setter
@NoArgsConstructor
public class IdentityDocument extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType type;
    @Column(nullable = false, name = "document_number")
    private String documentNumber;
    @Column(nullable = false, name = "issuing_country", length = 2)
    private String issuingCountry;
    @Column(name = "issue_date")
    private LocalDate issueDate;
    @Column(nullable = false, name = "expiry_date")
    private LocalDate expiryDate;
    @Column(name = "file_ref")
    private String fileRef;

    public IdentityDocument(DocumentType type, String documentNumber, String issuingCountry,
                            LocalDate issueDate, LocalDate expiryDate, String fileRef) {
        this.type = type;
        this.documentNumber = documentNumber;
        this.issuingCountry = issuingCountry;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
        this.fileRef = fileRef;
    }
}
