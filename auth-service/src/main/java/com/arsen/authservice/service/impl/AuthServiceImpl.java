package com.arsen.authservice.service.impl;

import com.arsen.authservice.exception.RegisterUserException;
import com.arsen.authservice.kafka.KafkaProduce;
import com.arsen.authservice.model.entity.User;
import com.arsen.authservice.model.enums.UserStatus;
import com.arsen.authservice.model.request.RegisterUserRequest;
import com.arsen.authservice.model.response.JwtResponse;
import com.arsen.authservice.repository.UserRepository;
import com.arsen.authservice.service.AuthService;
import com.arsen.authservice.service.JwtService;
import com.arsen.common.model.event.UserCreatedEvent;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaProduce kafkaProduce;
    private final JwtService jwtService;

    private static final String USER_CREATED_TOPIC = "user-created-topic";

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           KafkaProduce kafkaProduce,
                           JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.kafkaProduce = kafkaProduce;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public JwtResponse registerUser(RegisterUserRequest registerUserRequest) {
        if (userRepository.existsByEmail(registerUserRequest.email())) {
            throw new RegisterUserException("User with email %s already exists!".formatted(registerUserRequest.email()));
        }
        if (userRepository.existsByUsername(registerUserRequest.username())) {
            throw new RegisterUserException("User with username %s already exists!".formatted(registerUserRequest.username()));
        }
        User user = new User();
        user.setUsername(registerUserRequest.username());
        user.setEmail(registerUserRequest.email());
        user.setPassword(passwordEncoder.encode(registerUserRequest.password()));
        user.setUserStatus(UserStatus.ACTIVE);

        User savedUser = userRepository.save(user);

        UserCreatedEvent event = UserCreatedEvent.create(
                savedUser.getId(),
                savedUser.getUsername()
        );
        kafkaProduce.sendRegisterUserMessage(event);
        String accessToken = jwtService.generateToken(new CustomUserDetail(savedUser));
        return JwtResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationMillis(accessToken))
                .build();
    }
}
