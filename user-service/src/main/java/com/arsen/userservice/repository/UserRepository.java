package com.arsen.userservice.repository;

import com.arsen.userservice.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(@Param("username") String username);
    Optional<User> findByEmail(@Param("email") String email);
    Optional<User> findByUsernameAndEmail(@Param("username") String username, @Param("email") String email);

    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
}
