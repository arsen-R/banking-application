package com.arsen.userservice.repository;

import com.arsen.userservice.model.entity.UserProfile;
import com.arsen.userservice.util.DateUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase
class UserProfileRepositoryTest {
    @Autowired
    private UserProfileRepository userProfileRepository;

    @Test
    void testFindUserProfileByFirstNameShouldReturnUserProfileWhenFirstNameFound() {
        UserProfile userProfile = userProfileRepository.save(new UserProfile("Zara", "Brown", DateUtil.asDate(LocalDate.of(1991, 7, 12)), "+14847748731"));

        Optional<UserProfile> foundUserProfile = userProfileRepository.findByFirstName("Zara");

        assertTrue(foundUserProfile.isPresent());
        assertEquals(userProfile.getId(), foundUserProfile.get().getId());
        assertEquals(userProfile.getFirstName(), foundUserProfile.get().getFirstName());
        assertEquals(userProfile.getLastName(), foundUserProfile.get().getLastName());
        assertEquals(userProfile.getBirthday(), foundUserProfile.get().getBirthday());
        assertEquals(userProfile.getCellPhoneNumber(), foundUserProfile.get().getCellPhoneNumber());
    }

    @Test
    void testFindUserProfileByFirstNameShouldReturnEmptyWhenFirstNameNotFound() {
        userProfileRepository.save(new UserProfile("Mike", "Cook", DateUtil.asDate(LocalDate.of(2000, 1, 22)), "+17890096431"));

        Optional<UserProfile> foundUserProfile = userProfileRepository.findByFirstName("Arsen");

        assertTrue(foundUserProfile.isEmpty());
    }

    @Test
    void testFindUserProfileByLastNameShouldReturnUserProfileWhenLastNameFound() {
        UserProfile userProfile = userProfileRepository.save(new UserProfile("Sean", "McDonald", DateUtil.asDate(LocalDate.of(1981, 12, 12)), "+17607749310"));

        Optional<UserProfile> foundUserProfile = userProfileRepository.findByLastName("McDonald");

        assertTrue(foundUserProfile.isPresent());
        assertEquals(userProfile.getId(), foundUserProfile.get().getId());
        assertEquals(userProfile.getFirstName(), foundUserProfile.get().getFirstName());
        assertEquals(userProfile.getLastName(), foundUserProfile.get().getLastName());
        assertEquals(userProfile.getBirthday(), foundUserProfile.get().getBirthday());
        assertEquals(userProfile.getCellPhoneNumber(), foundUserProfile.get().getCellPhoneNumber());
    }

    @Test
    void testFindUserProfileByLastNameShouldReturnEmptyWhenLastNameNotFound() {
        userProfileRepository.save(new UserProfile("Katherina", "River", DateUtil.asDate(LocalDate.of(1999, 2, 2)), "+19932661279"));

        Optional<UserProfile> foundUserProfile = userProfileRepository.findByLastName("Andrew");

        assertTrue(foundUserProfile.isEmpty());
    }

    @Test
    void testFindUserProfileByFirstNameAndLastNameShouldReturnUserProfileWhenFirstNameAndLastNameFound() {
        UserProfile userProfile = userProfileRepository.save(new UserProfile("Mario", "Hope", DateUtil.asDate(LocalDate.of(1989, 8, 1)), "+11932361572"));

        Optional<UserProfile> foundUserProfile = userProfileRepository.findByFirstNameAndLastName("Mario", "Hope");

        assertTrue(foundUserProfile.isPresent());
        assertEquals(userProfile.getId(), foundUserProfile.get().getId());
        assertEquals(userProfile.getFirstName(), foundUserProfile.get().getFirstName());
        assertEquals(userProfile.getLastName(), foundUserProfile.get().getLastName());
        assertEquals(userProfile.getBirthday(), foundUserProfile.get().getBirthday());
        assertEquals(userProfile.getCellPhoneNumber(), foundUserProfile.get().getCellPhoneNumber());
    }

    @Test
    void testFindUserProfileByFirstNameAndLastNameShouldReturnEmptyWhenFirstNameAndLastNameNotFound() {
        userProfileRepository.save(new UserProfile("River", "Hope", DateUtil.asDate(LocalDate.of(1991, 1, 12)), "+11936371872"));

        Optional<UserProfile> foundUserProfile = userProfileRepository.findByFirstNameAndLastName("Maria", "McAlister");

        assertTrue(foundUserProfile.isEmpty());
    }

    @Test
    void testExistsUserProfileByFirstNameShouldReturnTrueWhenFirstNameExists() {
        userProfileRepository.save(new UserProfile("Andrew", "Lake", DateUtil.asDate(LocalDate.of(1990, 11, 30)), "+13218908189"));
        boolean result = userProfileRepository.existsByFirstName("Andrew");
        assertTrue(result);
    }

    @Test
    void testExistsUserProfileByFirstNameShouldReturnFalseWhenFirstNameNotExists() {
        userProfileRepository.save(new UserProfile("George", "Lincoln", DateUtil.asDate(LocalDate.of(2001, 2, 28)), "+11938193900"));
        boolean result = userProfileRepository.existsByFirstName("Andrew");
        assertFalse(result);
    }

    @Test
    void testExistsUserProfileByLastNameShouldReturnTrueWhenLastNameExists() {
        userProfileRepository.save(new UserProfile("Diana", "Fox", DateUtil.asDate(LocalDate.of(1993, 4, 2)), "+16771898390"));
        boolean result = userProfileRepository.existsByLastName("Fox");
        assertTrue(result);
    }

    @Test
    void testExistsUserProfileByLastNameShouldReturnFalseWhenLastNameNotExists() {
        userProfileRepository.save(new UserProfile("Richard", "Lawrence", DateUtil.asDate(LocalDate.of(1997, 1, 11)), "+15942349023"));
        boolean result = userProfileRepository.existsByLastName("Jared");
        assertFalse(result);
    }

    @Test
    void testExistsUserProfileByFirstNameAndLastNameShouldReturnTrueWhenFirstNameAndLastNameExists() {
        userProfileRepository.save(new UserProfile("Michael", "Brooks", DateUtil.asDate(LocalDate.of(1995, 10, 11)), "+16771898390"));
        boolean result = userProfileRepository.existsByFirstNameAndLastName("Michael", "Brooks");
        assertTrue(result);
    }

    @Test
    void testExistsUserProfileByFirstNameAndLastNameShouldReturnFalseWhenFirstNameAndLastNameNotExists() {
        userProfileRepository.save(new UserProfile("Emily", "Walsh", DateUtil.asDate(LocalDate.of(1993, 12, 21)), "+13431871893"));
        boolean result = userProfileRepository.existsByFirstNameAndLastName("Eric", "Brooks");
        assertFalse(result);
    }
}