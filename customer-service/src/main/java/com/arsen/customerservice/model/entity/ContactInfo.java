package com.arsen.customerservice.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactInfo {
    @Column(nullable = false, name = "phone")
    private String phone;
    @Column(nullable = false, name = "phone_verified")
    private boolean phoneVerified;
    @Column(nullable = false, name = "email")
    private String email;
}
