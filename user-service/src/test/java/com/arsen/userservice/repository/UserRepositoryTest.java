package com.arsen.userservice.repository;

import com.arsen.userservice.model.entity.User;
import com.arsen.userservice.model.entity.UserProfile;
import com.arsen.userservice.model.enums.RoleName;
import com.arsen.userservice.model.enums.UserStatus;
import com.arsen.userservice.util.DateUtil;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    void testFindUserByUsernameShouldReturnUserWhenFoundByUsername() {
        String authId = UUID.randomUUID().toString();
        UserProfile userProfile = new UserProfile("Arsen", "Rodyk", DateUtil.asDate(LocalDate.of(2000, 2, 29)), "+12445483485");
        User user = userRepository.save(new User(authId, userProfile));

        Optional<User> savedUser = userRepository.findUserByAuthId(authId);

        assertTrue(savedUser.isPresent());
        assertEquals(user.getId(), savedUser.get().getId());
        assertEquals(user.getAuthId(), savedUser.get().getAuthId());
        assertEquals(user.getUserProfile(), savedUser.get().getUserProfile());
    }

    @Test
    void testFindUserByUsernameShouldReturnEmptyWhenUsernameNotFound() {
        String authId = UUID.randomUUID().toString();
        String anotherAuthId = UUID.randomUUID().toString();
        UserProfile userProfile = new UserProfile("Wei", "Chen", DateUtil.asDate(LocalDate.of(2000, 7, 12)), "+12562672771");
        userRepository.save(new User(authId, userProfile));

        Optional<User> savedUser = userRepository.findUserByAuthId(anotherAuthId);

        assertTrue(savedUser.isEmpty());
    }

    @Test
    void testFindUserByEmailShouldReturnUserWhenFoundByEmail() {
        String authId = UUID.randomUUID().toString();
        UserProfile userProfile = new UserProfile("Maria", "Rodriguez", DateUtil.asDate(LocalDate.of(1991, 8, 29)), "+12125550102");
        User user = userRepository.save(new User(authId, "maria.rodriguez@example.com", userProfile));

        Optional<User> savedUser = userRepository.findByIdentifier("maria.rodriguez@example.com");

        assertTrue(savedUser.isPresent());
        assertEquals(user.getId(), savedUser.get().getId());
        assertEquals(user.getAuthId(), savedUser.get().getAuthId());
        assertEquals(user.getUserProfile(), savedUser.get().getUserProfile());
    }

    @Test
    void testFindUserByEmailShouldReturnEmptyWhenEmailNotFound() {
        String authId = UUID.randomUUID().toString();
        UserProfile userProfile = new UserProfile("Sofia", "Rossi", DateUtil.asDate(LocalDate.of(2003, 12, 31)), "+12125550110");
        userRepository.save(new User(authId, "sofia.rossi@example.com", userProfile));

        Optional<User> savedUser = userRepository.findByIdentifier("mario.rodriguez@example.com");

        assertTrue(savedUser.isEmpty());
    }

    @Test
    void testExistsUserByUsernameShouldReturnTrueWhenUsernameExists() {
        String authId = UUID.randomUUID().toString();
        UserProfile userProfile = new UserProfile("Noah", "Williams", DateUtil.asDate(LocalDate.of(1965, 5, 21)), "+12125550116");
        userRepository.save(new User(authId, userProfile));

        boolean isUserExistsByUsername = userRepository.existsByAuthId(authId);

        assertTrue(isUserExistsByUsername);
    }

    @Test
    void testExistsUserByUsernameShouldReturnFalseWhenUsernameNotExists() {
        String authId = UUID.randomUUID().toString();
        String anotherAuthId = UUID.randomUUID().toString();
        UserProfile userProfile = new UserProfile("Yuki", "Tanaka", DateUtil.asDate(LocalDate.of(1992, 9, 21)), "+12125550114");
        userRepository.save(new User(authId, userProfile));

        boolean isUserExistsByUsername = userRepository.existsByAuthId(anotherAuthId);

        assertFalse(isUserExistsByUsername);
    }

    @Test
    void testExistsUserByEmailShouldReturnTrueWhenEmailExists() {
        String authId = UUID.randomUUID().toString();

        UserProfile userProfile = new UserProfile("Ryan", "O'Brien", DateUtil.asDate(LocalDate.of(1993, 5, 21)), "+12125550130");
        userRepository.save(new User(authId, "ryan.obrien@example.com", userProfile));

        boolean isUserExistsByEmail = userRepository.existsByIdentifier("ryan.obrien@example.com");

        assertTrue(isUserExistsByEmail);
    }

    @Test
    void testExistsUserByEmailShouldReturnFalseWhenEmailNotExists() {
        String authId = UUID.randomUUID().toString();
        UserProfile userProfile = new UserProfile("Grace", "Nguyen", DateUtil.asDate(LocalDate.of(1996, 7, 9)), "+12125550125");
        userRepository.save(new User(authId, "grace.nguyen@example.com", userProfile));

        boolean isUserExistsByEmail = userRepository.existsByIdentifier("ingrid.larsen@example.com");

        assertFalse(isUserExistsByEmail);
    }
}