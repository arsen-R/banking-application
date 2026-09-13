package com.arsen.userservice.model.dto;

public record UserDto(
        String id,
        String authId,
        String identifier,
        UserProfileDto userProfile
) {
    public UserDto(String authId, String identifier, UserProfileDto userProfile) {
        this(null, authId, identifier, userProfile);
    }
}
