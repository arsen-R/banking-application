package com.arsen.customerservice.messaging.payload;

import java.time.LocalDate;

public record UserRegisteredPayload(
        String authUserId,
        String username,
        String email,
        String firstName,
        String middleName,
        String lastName,
        LocalDate dateOfBirth,
        String phone
) {
}
