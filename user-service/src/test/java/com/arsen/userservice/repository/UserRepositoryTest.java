package com.arsen.userservice.repository;

import com.arsen.userservice.model.entity.Role;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

    private Role customerRole;
    private Role adminRole;
    private Role employeeRole;

    @BeforeEach
    void setUp() {
        customerRole = roleRepository.save(new Role(RoleName.CUSTOMER));
        adminRole = roleRepository.save(new Role(RoleName.ADMIN));
        employeeRole = roleRepository.save(new Role(RoleName.EMPLOYEE));
    }

    @Test
    void testFindUserByUsernameShouldReturnUserWhenFoundByUsername() {
        UserProfile userProfile = new UserProfile("Arsen", "Rodyk", DateUtil.asDate(LocalDate.of(2000, 2, 29)), "+12445483485");
        User user = userRepository.save(new User("arodyk", "arodyk@example.com", "Tr0ub4dor$9", UserStatus.ACTIVE, userProfile, Set.of(customerRole, adminRole)));

        Optional<User> savedUser = userRepository.findByUsername("arodyk");

        assertTrue(savedUser.isPresent());
        assertEquals(user.getId(), savedUser.get().getId());
        assertEquals(user.getUsername(), savedUser.get().getUsername());
        assertEquals(user.getEmail(), savedUser.get().getEmail());
        assertEquals(user.getRoles(), savedUser.get().getRoles());
        assertEquals(user.getStatus(), savedUser.get().getStatus());
    }

    @Test
    void testFindUserByUsernameShouldReturnEmptyWhenUsernameNotFound() {
        UserProfile userProfile = new UserProfile("Wei", "Chen", DateUtil.asDate(LocalDate.of(2000, 7, 12)), "+12562672771");
        userRepository.save(new User("weichen", "weichen1989@example.com", "Qu1ckFox!22", UserStatus.ACTIVE, userProfile, Set.of(customerRole)));

        Optional<User> savedUser = userRepository.findByUsername("arsenrodyk");

        assertTrue(savedUser.isEmpty());
    }

    @Test
    void testFindUserByEmailShouldReturnUserWhenFoundByEmail() {
        UserProfile userProfile = new UserProfile("Maria", "Rodriguez", DateUtil.asDate(LocalDate.of(1991, 8, 29)), "+12125550102");
        User user = userRepository.save(new User("mrodriguez", "maria.rodriguez@example.com", "Zeph7r@Lime", UserStatus.ACTIVE, userProfile, Set.of(employeeRole)));

        Optional<User> savedUser = userRepository.findByEmail("maria.rodriguez@example.com");

        assertTrue(savedUser.isPresent());
        assertEquals(user.getId(), savedUser.get().getId());
        assertEquals(user.getUsername(), savedUser.get().getUsername());
        assertEquals(user.getEmail(), savedUser.get().getEmail());
        assertEquals(user.getRoles(), savedUser.get().getRoles());
        assertEquals(user.getStatus(), savedUser.get().getStatus());
    }

    @Test
    void testFindUserByEmailShouldReturnEmptyWhenEmailNotFound() {
        UserProfile userProfile = new UserProfile("Sofia", "Rossi", DateUtil.asDate(LocalDate.of(2003, 12, 31)), "+12125550110");
        userRepository.save(new User("srossi", "sofia.rossi@example.com", "P0lar!Bear4", UserStatus.ACTIVE, userProfile, Set.of(customerRole)));

        Optional<User> savedUser = userRepository.findByEmail("mario.rodriguez@example.com");

        assertTrue(savedUser.isEmpty());
    }

    @Test
    void testFindUserByUsernameAndEmailShouldReturnUserWhenUsernameAndEmailFound() {
        UserProfile userProfile = new UserProfile("Morgan", "Vera", DateUtil.asDate(LocalDate.of(1999, 6, 2)), "+12125550124");
        User user = new User("morgan.vera", "morgan.vera1993@example.com", "GraniteW0lf#", UserStatus.ACTIVE, userProfile, Set.of(customerRole));
        userRepository.save(user);

        Optional<User> savedUser = userRepository.findByUsernameAndEmail("morgan.vera", "morgan.vera1993@example.com");

        assertTrue(savedUser.isPresent());
        assertEquals(user.getId(), savedUser.get().getId());
        assertEquals(user.getUsername(), savedUser.get().getUsername());
        assertEquals(user.getEmail(), savedUser.get().getEmail());
        assertEquals(user.getRoles(), savedUser.get().getRoles());
        assertEquals(user.getStatus(), savedUser.get().getStatus());
    }

    @Test
    void testFindUserByUsernameAndEmailShouldReturnNullWhenUsernameAndEmailNotFound() {
        UserProfile userProfile = new UserProfile("Samuel", "Adeyemi", DateUtil.asDate(LocalDate.of(1979, 3, 10)), "+12125550124");
        userRepository.save(new User("sadeyemi", "samuel.adeyemi@example.com", "Ir0nGate#3", UserStatus.ACTIVE, userProfile, Set.of(customerRole)));

        Optional<User> savedUser = userRepository.findByUsernameAndEmail("dkim", "david.kim@example.com");

        assertTrue(savedUser.isEmpty());
    }

    @Test
    void testExistsUserByUsernameShouldReturnTrueWhenUsernameExists() {
        UserProfile userProfile = new UserProfile("Noah", "Williams", DateUtil.asDate(LocalDate.of(1965, 5, 21)), "+12125550116");
        userRepository.save(new User("nwilliams", "noah.williams@example.com", "Cobalt#42q", UserStatus.ACTIVE, userProfile, Set.of(employeeRole)));

        boolean isUserExistsByUsername = userRepository.existsByUsername("nwilliams");

        assertTrue(isUserExistsByUsername);
    }

    @Test
    void testExistsUserByUsernameShouldReturnFalseWhenUsernameNotExists() {
        UserProfile userProfile = new UserProfile("Yuki", "Tanaka", DateUtil.asDate(LocalDate.of(1992, 9, 21)), "+12125550114");
        userRepository.save(new User("ytanaka", "yuki.tanaka@example.com", "V3lvetSky!", UserStatus.ACTIVE, userProfile, Set.of(employeeRole, adminRole)));

        boolean isUserExistsByUsername = userRepository.existsByUsername("ceze");

        assertFalse(isUserExistsByUsername);
    }

    @Test
    void testExistsUserByEmailShouldReturnTrueWhenEmailExists() {
        UserProfile userProfile = new UserProfile("Ryan", "O'Brien", DateUtil.asDate(LocalDate.of(1993, 5, 21)), "+12125550130");
        userRepository.save(new User("robrien2", "ryan.obrien@example.com", "Th1stle&Wynd", UserStatus.ACTIVE, userProfile, Set.of(customerRole)));

        boolean isUserExistsByEmail = userRepository.existsByEmail("ryan.obrien@example.com");

        assertTrue(isUserExistsByEmail);
    }

    @Test
    void testExistsUserByEmailShouldReturnFalseWhenEmailNotExists() {
        UserProfile userProfile = new UserProfile("Grace", "Nguyen", DateUtil.asDate(LocalDate.of(1996, 7, 9)), "+12125550125");
        userRepository.save(new User("gnguyen", "grace.nguyen@example.com", "Lotus!Br1dge", UserStatus.ACTIVE, userProfile, Set.of(customerRole)));

        boolean isUserExistsByEmail = userRepository.existsByEmail("ingrid.larsen@example.com");

        assertFalse(isUserExistsByEmail);
    }

    @Test
    void testSaveUseShouldThrowExceptionWhenEmailIsNotValid() {
        UserProfile userProfile = new UserProfile("Sophia", "Elizabeth", DateUtil.asDate(LocalDate.of(1983, 11, 2)), "+12025550108");
        User user = new User("sophiamorgan08", "sophia.morgan08example", "Test@Sophia08!", UserStatus.ACTIVE, userProfile, Set.of(customerRole, employeeRole));

        assertThatThrownBy(() -> userRepository.saveAndFlush(user)).isInstanceOf(ConstraintViolationException.class);
    }
}