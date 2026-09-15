package com.arsen.userservice.model.entity;

import com.arsen.userservice.model.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String username;
    @Column(nullable = false, unique = true, updatable = false)
    @Email(message = "Email is not valid")
    private String email;
    @Column(nullable = false)
    private String password;
    @Enumerated(EnumType.STRING)
    private UserStatus status;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id", referencedColumnName = "id", nullable = false, unique = true)
    private UserProfile userProfile;
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public User(String username, String email, String password, UserStatus status) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.status = status;
    }
 public User(String id,String username, String email, String password, UserStatus status) {
        this.setId(id);
        this.username = username;
        this.email = email;
        this.password = password;
        this.status = status;
    }

    public User(String username, String email, String password, UserStatus status, UserProfile userProfile) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.status = status;
        this.userProfile = userProfile;
    }

    public User(String username, String email, String password, UserStatus status, Set<Role> roles) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.status = status;
        this.roles = roles;
    }

    public User(String id, String username, String email, String password, UserStatus status, UserProfile userProfile,Set<Role> roles) {
        this.setId(id);
        this.username = username;
        this.email = email;
        this.password = password;
        this.status = status;
        this.userProfile = userProfile;
        this.roles = roles;
    }
}
