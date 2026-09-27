package com.arsen.customerservice.model.entity;

import com.arsen.customerservice.model.enums.CustomerStatus;
import com.arsen.customerservice.model.enums.CustomerType;
import com.arsen.customerservice.model.enums.KycStatus;
import com.arsen.customerservice.model.enums.RiskLevel;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
public class Customer extends BaseEntity {
    @Column(nullable = false, unique = true, updatable = false, name = "auth_user_id")
    private String authUserId;
    @Column(nullable = false, unique = true, updatable = false, name = "customer_number")
    private String customerNumber;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerType type = CustomerType.INDIVIDUAL;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerStatus status = CustomerStatus.PENDING_KYC;
    @Embedded
    private PersonalInfo personalInfo;
    @Embedded
    private ContactInfo contactInfo;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "kyc_status")
    private KycStatus kycStatus = KycStatus.PENDING;
    @Column(name = "kyc_verified_at")
    private Instant kycVerifiedAt;
    @Column(name = "kyc_rejection_reason")
    private String kycRejectionReason;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "risk_level")
    private RiskLevel riskLevel = RiskLevel.LOW;
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> addresses = new ArrayList<>();
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IdentityDocument> documents = new ArrayList<>();

    public Customer(String authUserId, String customerNumber, PersonalInfo personalInfo, ContactInfo contactInfo) {
        this.authUserId = authUserId;
        this.customerNumber = customerNumber;
        this.personalInfo = personalInfo;
        this.contactInfo = contactInfo;
    }

    public void addAddress(Address address) {
        address.setCustomer(this);
        addresses.add(address);
    }

    public void addDocument(IdentityDocument document) {
        document.setCustomer(this);
        documents.add(document);
    }
}
