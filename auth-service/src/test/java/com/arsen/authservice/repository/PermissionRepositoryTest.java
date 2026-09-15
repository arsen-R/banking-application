package com.arsen.authservice.repository;

import com.arsen.authservice.model.entity.Permission;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase
class PermissionRepositoryTest {
    @Autowired
    private PermissionRepository permissionRepository;

    @Test
    void testFindByPermissionNameShouldReturnPermission() {
        Permission permission = new Permission("READ");
        permissionRepository.save(permission);
        var foundPermission = permissionRepository.findByPermissionName(permission.getPermissionName());

        assertTrue(foundPermission.isPresent());
        assertEquals(permission.getPermissionName(), foundPermission.get().getPermissionName());
    }

    @Test
    void testFindByIdShouldReturnEmptyWhenPermissionNotFound() {

        var foundPermission = permissionRepository.findByPermissionName("CREATE");

        assertTrue(foundPermission.isEmpty());
    }
}