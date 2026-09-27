package com.arsen.customerservice.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonalInfo {
    @Column(nullable = false, name = "first_name")
    private String firstName;
    @Column(name = "middle_name")
    private String middleName;
    @Column(nullable = false, name = "last_name")
    private String lastName;
    @Column(nullable = false, name = "date_of_birth")
    private LocalDate dateOfBirth;
    @Column(name = "nationality", length = 2)
    private String nationality;
    @Column(name = "tax_id")
    private String taxId;
}
