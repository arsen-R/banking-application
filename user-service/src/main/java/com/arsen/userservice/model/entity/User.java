package com.arsen.userservice.model.entity;

import com.arsen.common.model.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {
    private String authId;
    private String identifier;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id", referencedColumnName = "id", nullable = false, unique = true)
    private UserProfile userProfile;

    public User(String authId, UserProfile userProfile) {
        this.authId = authId;
        this.userProfile = userProfile;
    }

    public User(String id, String authId, String identifier, UserProfile userProfile) {
        this.setId(id);
        this.authId = authId;
        this.identifier = identifier;
        this.userProfile = userProfile;
    }
}
