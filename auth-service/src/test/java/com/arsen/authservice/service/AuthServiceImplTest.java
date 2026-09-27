package com.arsen.authservice.service;

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
import com.arsen.authservice.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private OutboxWriter outboxWriter;
    @InjectMocks
    private AuthServiceImpl authService;

    private final RegisterRequest request = new RegisterRequest("emily", "emily@example.com", "Secret123!",
            "Emily", null, "Ramirez", LocalDate.of(1990, 5, 20), "+12025550124");

    @Test
    void registerShouldSavePendingUserAndWriteUserRegisteredEvent() {
        Role customerRole = new Role(RoleName.CUSTOMER, new HashSet<>());
        when(userRepository.existsByUsername("emily")).thenReturn(false);
        when(userRepository.existsByEmail("emily@example.com")).thenReturn(false);
        when(roleRepository.findByRoleName(RoleName.CUSTOMER)).thenReturn(Optional.of(customerRole));
        when(passwordEncoder.encode("Secret123!")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId("user-1");
            return u;
        });

        RegisterResponse response = authService.register(request);

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getPassword()).isEqualTo("hashed");
        assertThat(saved.getValue().getUserStatus()).isEqualTo(UserStatus.PENDING_VERIFICATION);
        assertThat(saved.getValue().getIsEnabled()).isFalse();
        assertThat(saved.getValue().getRoles()).containsExactly(customerRole);
        assertThat(response).isEqualTo(new RegisterResponse("user-1", "emily", "emily@example.com", UserStatus.PENDING_VERIFICATION));
        verify(outboxWriter).write(Topics.AUTH_USER_EVENTS, "User", "user-1", EventTypes.USER_REGISTERED,
                new UserRegisteredPayload("user-1", "emily", "emily@example.com", "Emily", null, "Ramirez",
                        LocalDate.of(1990, 5, 20), "+12025550124"));
    }

    @Test
    void registerShouldRejectDuplicateUsername() {
        when(userRepository.existsByUsername("emily")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request)).isInstanceOf(UserAlreadyExistsException.class);
        verify(userRepository, never()).save(any());
        verifyNoInteractions(outboxWriter);
    }

    @Test
    void activateUserShouldEnablePendingUser() {
        User user = new User("emily", "emily@example.com", "hashed", UserStatus.PENDING_VERIFICATION, Set.of());
        user.setIsEnabled(false);
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        authService.activateUser("user-1");

        assertThat(user.getUserStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getIsEnabled()).isTrue();
    }

    @Test
    void activateUserShouldNotReactivateDisabledUser() {
        User user = new User("emily", "emily@example.com", "hashed", UserStatus.DISABLED, Set.of());
        user.setIsEnabled(false);
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        authService.activateUser("user-1");

        assertThat(user.getUserStatus()).isEqualTo(UserStatus.DISABLED);
        assertThat(user.getIsEnabled()).isFalse();
    }

    @Test
    void disableUserShouldDisableUser() {
        User user = new User("emily", "emily@example.com", "hashed", UserStatus.PENDING_VERIFICATION, Set.of());
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        authService.disableUser("user-1", "underage");

        assertThat(user.getUserStatus()).isEqualTo(UserStatus.DISABLED);
        assertThat(user.getIsEnabled()).isFalse();
    }
}
