package com.arsen.customerservice.model.request;

import jakarta.validation.constraints.NotBlank;

public record KycRejectRequest(
        @NotBlank(message = "The rejection reason is required")
        String reason
) {
}
