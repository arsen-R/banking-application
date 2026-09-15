package com.arsen.authservice.model.request;

import com.arsen.common.model.component.annotation.CellPhoneNumber;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

import java.util.Date;

public record RegisterUserRequest(
        @NotBlank(message = "The username is required")
        String username,
        @NotBlank(message = "The email is required")
        @Email(message = "Email address is not valid")
        String email,
        @NotBlank(message = "The password is required")
        @Size(min = 8, message = "Must be at least 8 characters")
        String password,
        String authId,
        @NotBlank(message = "The first name is required")
        String firstName,
        String middleName,
        @NotBlank(message = "The last name is required")
        String lastName,
        @NotNull(message = "The birthday is required")
        @Past(message = "The birthday must be in the past")
        @JsonFormat(pattern = "dd/MM/yyyy")
        Date birthday,
        @NotBlank(message = "The cell phone number is required")
        @CellPhoneNumber
        String cellPhoneNumber
) {
}
