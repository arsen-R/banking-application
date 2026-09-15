package com.arsen.userservice.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

public record UserUpdateRequest(
        @NotBlank(message = "This username is required")
        String username,
        @NotBlank(message = "The first name is required")
        String firstName,
        String middleName,
        @NotBlank(message = "The last name is required")
        String lastName,
        @NotNull(message = "The birthday is required")
        Date birthday,
        @NotBlank(message = "The cell phone number is required")
        String cellPhoneNumber
) {
}
