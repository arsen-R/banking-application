package com.arsen.customerservice.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateCustomerRequest(
        @NotBlank(message = "The first name is required")
        String firstName,
        String middleName,
        @NotBlank(message = "The last name is required")
        String lastName,
        @Size(min = 2, max = 2, message = "Nationality must be an ISO 3166-1 alpha-2 code")
        String nationality,
        String taxId,
        @NotBlank(message = "The email is required")
        @Email(message = "Email address is not valid")
        String email,
        @NotBlank(message = "The phone number is required")
        @Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "Phone number must be in E.164 format")
        String phone
) {
}
