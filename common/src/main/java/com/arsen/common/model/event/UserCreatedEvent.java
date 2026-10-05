package com.arsen.common.model.event;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Data
@Builder
public class UserCreatedEvent {
    private String eventId;
    private String authId;
    private String username;
    private Date createdAt;

    public static UserCreatedEvent create(String authId, String username) {
        return UserCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .authId(authId)
                .username(username)
                .createdAt(Date.from(Instant.now()))
                .build();
    }
}
