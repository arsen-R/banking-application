package com.arsen.authservice.controller;

import com.arsen.authservice.model.request.RegisterUserRequest;
import com.arsen.authservice.model.response.JwtResponse;
import com.arsen.authservice.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<JwtResponse> registerUser(@Valid @RequestBody RegisterUserRequest registerUserRequest) {
        var result = authService.registerUser(registerUserRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
