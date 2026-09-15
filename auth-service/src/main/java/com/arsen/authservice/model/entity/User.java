package com.arsen.authservice.model.entity;

import com.arsen.authservice.model.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
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
    private UserStatus userStatus;
    @Column(nullable = false, name = "is_account_non_expired")
    private Boolean isAccountNonExpired = true;
    @Column(nullable = false, name = "is_account_non_locked")
    private Boolean isAccountNonLocked = true;
    @Column(nullable = false, name = "is_credentials_non_expired")
    private Boolean isCredentialsNonExpired = true;
    @Column(nullable = false, name = "is_enable")
    private Boolean isEnabled = true;
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    public User(String id, String username, String email, String password, UserStatus userStatus, Set<Role> roles) {
        this.setId(id);
        this.username = username;
        this.email = email;
        this.password = password;
        this.userStatus = userStatus;
        this.roles = roles;
    }

    public User(String username, String email, String password, UserStatus userStatus, Set<Role> roles) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.userStatus = userStatus;
        this.roles = roles;
    }

    public User(String id,
                String username,
                String email,
                String password,
                UserStatus userStatus,
                Boolean isAccountNonExpired,
                Boolean isAccountNonLocked,
                Boolean isCredentialsNonExpired,
                Boolean isEnabled, Set<Role> roles) {
        this.setId(id);
        this.username = username;
        this.email = email;
        this.password = password;
        this.userStatus = userStatus;
        this.isAccountNonExpired = isAccountNonExpired;
        this.isAccountNonLocked = isAccountNonLocked;
        this.isCredentialsNonExpired = isCredentialsNonExpired;
        this.isEnabled = isEnabled;
        this.roles = roles;
    }


}
