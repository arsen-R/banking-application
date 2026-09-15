package com.arsen.authservice.model.dto;

import com.arsen.authservice.model.enums.UserStatus;

import java.util.Set;

public record UserDto(
        String username,
        String email,
        String password,
        UserStatus userStatus,
        Boolean isAccountNonExpired,
        Boolean isAccountNonLocked,
        Boolean isCredentialsNonExpired,
        Boolean isEnabled,
        Set<RoleDto> roles
) {
}
