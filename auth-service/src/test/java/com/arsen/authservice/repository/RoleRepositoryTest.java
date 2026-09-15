package com.arsen.authservice.repository;

import com.arsen.authservice.model.entity.Permission;
import com.arsen.authservice.model.entity.Role;
import com.arsen.authservice.model.enums.RoleName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase
class RoleRepositoryTest {
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PermissionRepository permissionRepository;

    private Permission permission;
    @BeforeEach
    void setUp() {
        permission = permissionRepository.saveAndFlush(new Permission("READ"));
    }

    @Test
    void testFindByRoleNameShouldReturnRole() {
        Role adminRole = new Role(RoleName.ADMIN, Set.of(permission));
        roleRepository.save(adminRole);

        Optional<Role> foundRole = roleRepository.findByRoleName(adminRole.getRoleName());

        assertNotNull(foundRole);
        assertTrue(foundRole.isPresent());
        assertEquals(adminRole.getRoleName(), foundRole.get().getRoleName());
    }

    @Test
    void testFindByRoleNameShouldReturnNullWhenNotFound() {
        Optional<Role> foundRole = roleRepository.findByRoleName(RoleName.TELLER);

        assertTrue(foundRole.isEmpty());
    }

    @Test
    void testExistsByRoleNameShouldReturnTrue() {
        Role adminRole = new Role(RoleName.CUSTOMER, Set.of());
        roleRepository.save(adminRole);

        Boolean existsByRoleName = roleRepository.existsByRoleName(adminRole.getRoleName());

        assertTrue(existsByRoleName);
    }

    @Test
    void testExistsByRoleNameShouldReturnFalseWhenNotExists() {
        Boolean existsByRoleName = roleRepository.existsByRoleName(RoleName.TELLER);
        assertFalse(existsByRoleName);
    }
}