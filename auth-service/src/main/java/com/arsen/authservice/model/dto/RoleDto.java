package com.arsen.authservice.model.dto;

import com.arsen.authservice.model.enums.RoleName;

import java.util.Set;

public record RoleDto(
        RoleName roleName,
        Set<PermissionDto> permission
) {
}
