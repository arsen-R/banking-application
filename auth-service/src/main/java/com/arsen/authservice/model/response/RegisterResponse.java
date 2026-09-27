package com.arsen.authservice.model.response;

import com.arsen.authservice.model.enums.UserStatus;

public record RegisterResponse(String userId, String username, String email, UserStatus status) {
}
