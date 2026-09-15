package com.arsen.userservice.model.request;

import com.arsen.userservice.model.dto.RoleDto;
import com.arsen.userservice.component.annotation.CellPhoneNumber;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

import java.util.Date;
import java.util.Set;

public record CreateUserRequest(
        @NotBlank(message = "The username is required")
        String username,
        @NotBlank(message = "The email is required")
        @Email(message = "Email address is not valid")
        String email,
        @NotBlank(message = "The password is required")
        @Size(min = 8, message = "Must be at least 8 characters")
        String password,
        @NotEmpty(message = "At least one role is required")
        Set<RoleDto> roles,
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
    public CreateUserRequest(String username, String email, String password, Set<RoleDto> roles, String firstName, String lastName, Date birthday, String cellPhoneNumber) {
        this(username, email, password, roles, firstName, "", lastName, birthday, cellPhoneNumber);
    }
}
