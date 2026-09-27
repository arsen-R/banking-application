package com.arsen.customerservice.model.request;

import com.arsen.customerservice.model.enums.AddressType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotNull(message = "The address type is required")
        AddressType type,
        @NotBlank(message = "The address line is required")
        String line1,
        String line2,
        @NotBlank(message = "The city is required")
        String city,
        String region,
        @NotBlank(message = "The postal code is required")
        String postalCode,
        @NotBlank(message = "The country is required")
        @Size(min = 2, max = 2, message = "Country must be an ISO 3166-1 alpha-2 code")
        String country
) {
}
