package com.arsen.authservice.service;

import com.arsen.authservice.model.request.RegisterUserRequest;
import com.arsen.authservice.model.response.JwtResponse;

public interface AuthService {
    JwtResponse registerUser(RegisterUserRequest registerUserRequest);
}
