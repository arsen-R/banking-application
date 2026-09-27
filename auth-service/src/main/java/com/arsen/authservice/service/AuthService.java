package com.arsen.authservice.service;

import com.arsen.authservice.model.request.RegisterRequest;
import com.arsen.authservice.model.response.RegisterResponse;

public interface AuthService {
    RegisterResponse register(RegisterRequest request);

    void activateUser(String userId);

    void disableUser(String userId, String reason);
}
