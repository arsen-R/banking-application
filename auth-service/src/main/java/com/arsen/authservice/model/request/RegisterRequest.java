package com.arsen.authservice.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegisterRequest(
        @NotBlank(message = "The username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,
        @NotBlank(message = "The email is required")
        @Email(message = "Email address is not valid")
        String email,
        @NotBlank(message = "The password is required")
        @Size(min = 8, message = "Must be at least 8 characters")
        String password,
        @NotBlank(message = "The first name is required")
        String firstName,
        String middleName,
        @NotBlank(message = "The last name is required")
        String lastName,
        @NotNull(message = "The date of birth is required")
        @Past(message = "The date of birth must be in the past")
        LocalDate dateOfBirth,
        @NotBlank(message = "The phone number is required")
        @Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "Phone number must be in E.164 format")
        String phone
) {
}
