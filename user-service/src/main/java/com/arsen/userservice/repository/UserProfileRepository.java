package com.arsen.userservice.repository;

import com.arsen.userservice.model.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, String> {
    Optional<UserProfile> findByFirstName(String firstName);
    Optional<UserProfile> findByLastName(String lastName);
    Optional<UserProfile> findByFirstNameAndLastName(String firstName, String lastName);

    boolean existsByFirstName(String firstName);
    boolean existsByLastName(String lastName);
    boolean existsByFirstNameAndLastName(String firstName, String lastName);
}
