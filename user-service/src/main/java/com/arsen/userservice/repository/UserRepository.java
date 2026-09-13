package com.arsen.userservice.repository;

import com.arsen.userservice.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findUserByAuthId(String authId);
    Optional<User> findByIdentifier(String identifier);
    boolean existsByAuthId(String authId);
    boolean existsByIdentifier(String identifier);
}
