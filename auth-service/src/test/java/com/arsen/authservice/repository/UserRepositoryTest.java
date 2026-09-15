package com.arsen.authservice.repository;

import com.arsen.authservice.model.entity.Role;
import com.arsen.authservice.model.entity.User;
import com.arsen.authservice.model.enums.RoleName;
import com.arsen.authservice.model.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.AutoConfigureDataJpa;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureDataJpa
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

    private Role customerRole;
    private Role adminRole;
    private Role tellerRole;

    @BeforeEach
    void setUp() {
        customerRole = roleRepository.save(new Role(RoleName.CUSTOMER, Set.of()));
        adminRole = roleRepository.save(new Role(RoleName.ADMIN, Set.of()));
        tellerRole = roleRepository.save(new Role(RoleName.TELLER, Set.of()));
    }

    @Test
    void testFindUserByUsernameShouldReturnUserWhenFoundByUsername() {
        User user = userRepository.save(new User("ytanaka", "yuki.tanaka@example.com", "Tr0ub4dor$9", UserStatus.ACTIVE, Set.of(customerRole, adminRole)));

        Optional<User> savedUser = userRepository.findByUsername("ytanaka");

        assertTrue(savedUser.isPresent());
        assertEquals(user.getId(), savedUser.get().getId());
        assertEquals(user.getUsername(), savedUser.get().getUsername());
        assertEquals(user.getEmail(), savedUser.get().getEmail());
        assertEquals(user.getRoles(), savedUser.get().getRoles());
        assertEquals(user.getUserStatus(), savedUser.get().getUserStatus());
    }

    @Test
    void testFindUserByUsernameShouldReturnEmptyWhenUsernameNotFound() {
        Optional<User> savedUser = userRepository.findByUsername("weichen");

        assertTrue(savedUser.isEmpty());
    }

    @Test
    void testFindUserByEmailShouldReturnUserWhenFoundByEmail() {
        User user = userRepository.save(new User("mrodriguez", "maria.rodriguez@example.com", "Zeph7r@Lime", UserStatus.ACTIVE, Set.of(tellerRole)));

        Optional<User> savedUser = userRepository.findByEmail("maria.rodriguez@example.com");

        assertTrue(savedUser.isPresent());
        assertEquals(user.getId(), savedUser.get().getId());
        assertEquals(user.getUsername(), savedUser.get().getUsername());
        assertEquals(user.getEmail(), savedUser.get().getEmail());
        assertEquals(user.getRoles(), savedUser.get().getRoles());
        assertEquals(user.getUserStatus(), savedUser.get().getUserStatus());
    }

    @Test
    void testFindUserByEmailShouldReturnEmptyWhenEmailNotFound() {
        Optional<User> savedUser = userRepository.findByEmail("mario.rodriguez@example.com");

        assertTrue(savedUser.isEmpty());
    }

    @Test
    void testFindByUsernameOrEmailShouldReturnUserWhenFoundByUsernameOrEmail() {
        User user = userRepository.saveAndFlush(
                new User("gnguyen", "grace.nguyen@example.com", "Lotus!Br1dge", UserStatus.ACTIVE, Set.of(customerRole)));

        Optional<User> foundUserByUsername = userRepository.findByUsernameOrEmail("gnguyen");
        Optional<User> foundUserByEmail = userRepository.findByUsernameOrEmail("grace.nguyen@example.com");

        assertTrue(foundUserByUsername.isPresent());
        assertTrue(foundUserByEmail.isPresent());
        assertEquals(user.getUsername(), foundUserByUsername.get().getUsername());
        assertEquals(user.getEmail(), foundUserByEmail.get().getEmail());
        assertEquals(foundUserByEmail.get(), foundUserByUsername.get());
    }

    @Test
    void testFindByUsernameOrEmailShouldReturnEmptyWhenUsernameOrEmailNotFound() {
        Optional<User> foundUserByUsername = userRepository.findByUsernameOrEmail("sophiamorgan08");
        Optional<User> foundUserByEmail = userRepository.findByUsernameOrEmail("sophia.morgan08@example.com");
        assertTrue(foundUserByUsername.isEmpty());
        assertTrue(foundUserByEmail.isEmpty());
    }

    @Test
    void testExistsUserByUsernameShouldReturnTrueWhenUsernameExists() {
        userRepository.save(new User("nwilliams", "noah.williams@example.com", "Cobalt#42q", UserStatus.ACTIVE, Set.of(tellerRole)));

        boolean isUserExistsByUsername = userRepository.existsByUsername("nwilliams");

        assertTrue(isUserExistsByUsername);
    }

    @Test
    void testExistsUserByUsernameShouldReturnFalseWhenUsernameNotExists() {
        boolean isUserExistsByUsername = userRepository.existsByUsername("ceze");

        assertFalse(isUserExistsByUsername);
    }

    @Test
    void testExistsUserByEmailShouldReturnTrueWhenEmailExists() {
       userRepository.saveAndFlush(new User("morgan.vera", "morgan.vera1993@example.com", "GraniteW0lf#", UserStatus.ACTIVE, Set.of(customerRole)));

        boolean isEmailExist = userRepository.existsByEmail("morgan.vera1993@example.com");

        assertTrue(isEmailExist);
    }

    @Test
    void testExistsUserByEmailShouldReturnFalseWhenEmailNotExists() {
        boolean isEmailExist = userRepository.existsByEmail("morgan.vera1993@example.com");

        assertFalse(isEmailExist);
    }
}