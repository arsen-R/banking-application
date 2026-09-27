package com.arsen.authservice.service.impl;

import com.arsen.authservice.exception.UserAlreadyExistsException;
import com.arsen.authservice.messaging.EventTypes;
import com.arsen.authservice.messaging.Topics;
import com.arsen.authservice.messaging.outbox.OutboxWriter;
import com.arsen.authservice.messaging.payload.UserRegisteredPayload;
import com.arsen.authservice.model.entity.Role;
import com.arsen.authservice.model.entity.User;
import com.arsen.authservice.model.enums.RoleName;
import com.arsen.authservice.model.enums.UserStatus;
import com.arsen.authservice.model.request.RegisterRequest;
import com.arsen.authservice.model.response.RegisterResponse;
import com.arsen.authservice.repository.RoleRepository;
import com.arsen.authservice.repository.UserRepository;
import com.arsen.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private static final String USER_AGGREGATE = "User";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final OutboxWriter outboxWriter;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UserAlreadyExistsException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("Email is already registered");
        }
        Role customerRole = roleRepository.findByRoleName(RoleName.CUSTOMER)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.CUSTOMER, new HashSet<>())));

        User user = new User(request.username(), request.email(), passwordEncoder.encode(request.password()),
                UserStatus.PENDING_VERIFICATION, new HashSet<>(Set.of(customerRole)));
        user.setIsEnabled(false);
        User savedUser = userRepository.save(user);

        outboxWriter.write(Topics.AUTH_USER_EVENTS, USER_AGGREGATE, savedUser.getId(), EventTypes.USER_REGISTERED,
                new UserRegisteredPayload(savedUser.getId(), savedUser.getUsername(), savedUser.getEmail(),
                        request.firstName(), request.middleName(), request.lastName(),
                        request.dateOfBirth(), request.phone()));

        return new RegisterResponse(savedUser.getId(), savedUser.getUsername(), savedUser.getEmail(), savedUser.getUserStatus());
    }

    @Override
    @Transactional
    public void activateUser(String userId) {
        userRepository.findById(userId).ifPresentOrElse(user -> {
            if (user.getUserStatus() == UserStatus.PENDING_VERIFICATION) {
                user.setUserStatus(UserStatus.ACTIVE);
                user.setIsEnabled(true);
            }
        }, () -> log.warn("Cannot activate unknown user {}", userId));
    }

    @Override
    @Transactional
    public void disableUser(String userId, String reason) {
        userRepository.findById(userId).ifPresentOrElse(user -> {
            log.info("Disabling user {}: {}", userId, reason);
            user.setUserStatus(UserStatus.DISABLED);
            user.setIsEnabled(false);
        }, () -> log.warn("Cannot disable unknown user {}", userId));
    }
}
