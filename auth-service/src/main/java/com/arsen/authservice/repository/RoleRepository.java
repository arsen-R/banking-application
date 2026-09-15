package com.arsen.authservice.repository;

import com.arsen.authservice.model.entity.Role;
import com.arsen.authservice.model.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {
    Optional<Role> findByRoleName(RoleName roleName);
    Boolean existsByRoleName(RoleName roleName);
}
