package com.arsen.userservice.service;

import com.arsen.userservice.exception.CreateUserException;
import com.arsen.userservice.exception.UserNotFoundException;
import com.arsen.userservice.model.dto.UserDto;
import com.arsen.userservice.model.dto.UserProfileDto;
import com.arsen.userservice.model.entity.User;
import com.arsen.userservice.model.entity.UserProfile;
import com.arsen.userservice.model.mapper.UserMapper;
import com.arsen.userservice.model.request.CreateUserRequest;
import com.arsen.userservice.model.request.UserUpdateRequest;
import com.arsen.userservice.model.response.PageResponse;
import com.arsen.userservice.repository.UserRepository;
import com.arsen.userservice.util.DateUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;

    @Test
    void testFindAllUsersShouldReturnAllUsersWhenUsersAreFound() {
        int page = 0;
        int pageSize = 3;
        List<User> users = List.of(
                new User(UUID.randomUUID().toString(), new UserProfile("Zara", "Brown", DateUtil.asDate(LocalDate.of(1991, 7, 12)), "+14847748731")),
                new User(UUID.randomUUID().toString(), new UserProfile("Sean", "McDonald", DateUtil.asDate(LocalDate.of(1981, 12, 12)), "+17607749310")),
                new User(UUID.randomUUID().toString(), new UserProfile("Katherina", "River", DateUtil.asDate(LocalDate.of(1999, 2, 2)), "+19932661279"))
        );
        Page<User> userPage = new PageImpl<>(users, PageRequest.of(page, pageSize), users.size());

        List<UserDto> userDtos = users.stream().map(userMapper::userToUserDto).toList();

        when(userRepository.findAll(PageRequest.of(page, pageSize))).thenReturn(userPage);
        for (int i = 0; i < users.size(); i++) {
            when(userMapper.userToUserDto(users.get(i))).thenReturn(userDtos.get(i));
        }

        PageResponse<UserDto> foundAllUsers = userService.findAllUsers(0, 3);

        assertThat(foundAllUsers.getContent()).isEqualTo(userDtos);
        assertThat(foundAllUsers.getPage()).isEqualTo(page);
        assertThat(foundAllUsers.getPageSize()).isEqualTo(pageSize);
        assertThat(foundAllUsers.getTotalElements()).isEqualTo(users.size());
        assertThat(foundAllUsers.getTotalPages()).isEqualTo(1);

        verify(userRepository).findAll(PageRequest.of(page, pageSize));
    }

    @Test
    void testFindAllUsersShouldReturnEmptyListWhenUsersUserNotExist() {
        int page = 0;
        int pageSize = 10;
        Page<User> emptyPage = new PageImpl<>(List.of(), PageRequest.of(page, pageSize), 0);

        when(userRepository.findAll(PageRequest.of(page, pageSize))).thenReturn(emptyPage);

        PageResponse<UserDto> foundAllUsers = userService.findAllUsers(page, pageSize);

        assertThat(foundAllUsers.getContent()).isEmpty();
        assertThat(foundAllUsers.getTotalElements()).isZero();
        verify(userRepository).findAll(PageRequest.of(page, pageSize));
        verifyNoInteractions(userMapper);
    }

    @Test
    void testFindUserByIdShouldReturnUserWhenUserFound() {
        String authId = UUID.randomUUID().toString();
        User user = new User(authId, new UserProfile("Morgan", "Vera", DateUtil.asDate(LocalDate.of(2002, 1, 12)), "+12125550124"));

        UserDto userDto = new UserDto(authId, "morgan.vera@example.com", new UserProfileDto("Morgan", "Vera", DateUtil.asDate(LocalDate.of(2002, 1, 12)), "+12125550124"));

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userMapper.userToUserDto(user)).thenReturn(userDto);

        UserDto foundUser = userService.findUserById(user.getId());

        assertEquals(userDto.id(), foundUser.id());
        assertEquals(userDto.authId(), foundUser.authId());
        assertEquals(userDto.userProfile(), foundUser.userProfile());

        verify(userRepository).findById(user.getId());
        verify(userMapper).userToUserDto(user);
    }

    @Test
    void testFindUserByIdShouldThrowExceptionWhenUserIsNotFound() {
        String userId = UUID.randomUUID().toString();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.findUserById(userId));
    }

    @Test
    void testFindUserByIdShouldThrowExceptionWhenUserIdIsBlank() {
        when(userRepository.findById("")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.findUserById(""));
    }

    @Test
    void testFindUserByIdShouldThrowExceptionWhenUserIdIsNullable() {
        when(userRepository.findById(null)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.findUserById(null));

    }

    @Test
    void testFindUserByEmailShouldReturnUserWhenUserFoundAndEmailIsValid() {
        String authId = UUID.randomUUID().toString();

        User user = new User(authId,
                new UserProfile("Liam", "James", "Carter", DateUtil.asDate(LocalDate.of(1994, 9, 25)), "+12025550101")
        );

        UserDto userDto = new UserDto(authId, "",
                new UserProfileDto("Liam", "James", "Carter", DateUtil.asDate(LocalDate.of(1994, 9, 25)), "+12025550101")
        );

        when(userRepository.findByIdentifier(user.getIdentifier())).thenReturn(Optional.of(user));
        when(userMapper.userToUserDto(user)).thenReturn(userDto);

        UserDto foundUser = userService.findUserByIdentifier(user.getIdentifier());

        assertThat(foundUser).isEqualTo(userDto);
        assertEquals(userDto.authId(), foundUser.authId());
        assertEquals(userDto.identifier(), foundUser.identifier());
        assertEquals(userDto.userProfile(), foundUser.userProfile());

        verify(userRepository).findByIdentifier(user.getIdentifier());
        verify(userMapper).userToUserDto(user);
    }

    @Test
    void testFindUserByEmailShouldThrowExceptionWhenUserIsNotFound() {
        String email = "sophie.grant84@example.com";
        when(userRepository.findByIdentifier(email)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.findUserByIdentifier(email));

        verify(userRepository).findByIdentifier(email);
    }

    @Test
    void testFindUserByEmailShouldThrowExceptionWhenEmailIsBlank() {
        when(userRepository.findByIdentifier("")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.findUserByIdentifier(""));

        verify(userRepository).findByIdentifier("");
    }

    @Test
    void testFindUserByEmailShouldThrowExceptionWhenEmailIsNull() {
        when(userRepository.findByIdentifier(null)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.findUserByIdentifier(null));

        verify(userRepository).findByIdentifier(null);
    }

    @Test
    void testFindUserByEmailShouldThrowExceptionWhenEmailIsNotValid() {
        String email = "emma.mitchell02example";
        when(userRepository.findByIdentifier(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUserByIdentifier(email)).isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findByIdentifier(email);
    }

    @Test
    void testCreateUserShouldReturnUserWhenUserIsNotExist() {
        String authId = UUID.randomUUID().toString();

        CreateUserRequest createUserRequest = new CreateUserRequest("emilyramirez24", "emily.ramirez24@example.com", "Test@Emily24!", authId,"Emily", "Sophia", "Ramirez", DateUtil.asDate(LocalDate.of(1994, 12, 2)), "+12025550124");

        UserProfile userProfile = new UserProfile("Emily", "Sophia", "Ramirez", DateUtil.asDate(LocalDate.of(1994, 12, 2)), "+12025550124");
        User user = new User(authId, "emilyramirez24", userProfile);

        UserProfileDto userProfileDto = new UserProfileDto("Emily", "Sophia", "Ramirez", DateUtil.asDate(LocalDate.of(1994, 12, 2)), "+12025550124");
        UserDto userDto = new UserDto(authId, "emily.ramirez24@example.com", userProfileDto);

        when(userRepository.existsByAuthId(createUserRequest.authId())).thenReturn(false);
        when(userRepository.saveAndFlush(user)).thenReturn(user);
        when(userMapper.userToUserDto(user)).thenReturn(userDto);

        UserDto createdUserDto = userService.createUser(createUserRequest);

        assertEquals(userDto.authId(), createdUserDto.authId());
        assertEquals(userDto.identifier(), createdUserDto.identifier());
        assertEquals(userDto.userProfile().firstName(), createdUserDto.userProfile().firstName());
        assertEquals(userDto.userProfile().middleName(), createdUserDto.userProfile().middleName());
        assertEquals(userDto.userProfile().lastName(), createdUserDto.userProfile().lastName());
        assertEquals(userDto.userProfile().birthday(), createdUserDto.userProfile().birthday());
        assertEquals(userDto.userProfile().cellPhoneNumber(), createdUserDto.userProfile().cellPhoneNumber());

        verify(userRepository).existsByAuthId(createUserRequest.authId());
        verify(userRepository).saveAndFlush(user);
        verify(userMapper).userToUserDto(user);
    }

    @Test
    void testCreateUserShouldThrowExceptionWhenUserIsExist() {
        String authId = UUID.randomUUID().toString();
        CreateUserRequest createUserRequest = new CreateUserRequest("lucasturner09", "lucas.turner09@example.com", "Test@Lucas09!", authId, "Lucas", "William", DateUtil.asDate(LocalDate.of(1984, 2, 25)), "+12025550109");

        when(userRepository.existsByAuthId(createUserRequest.authId())).thenReturn(true);

        assertThrows(CreateUserException.class, () -> userService.createUser(createUserRequest));

        verify(userRepository).existsByAuthId(createUserRequest.authId());
    }

    @Test
    void testCreateUserShouldThrowExceptionWhenUserIsNull() {
        assertThrows(NullPointerException.class, () -> userService.createUser(null));
    }

    @Test
    void testUpdateUserByIdShouldReturnUserWhenUserIsUpdated() {
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest(
                "benjamincooper13", "Benjamin", "Thomas", "Cooper", DateUtil.asDate(LocalDate.of(1998, 12, 12)), "+12025550113"
        );
        String userId = UUID.randomUUID().toString();
        String authId = UUID.randomUUID().toString();

        User user = new User(userId, authId, "benjamincooper13",
                new UserProfile("Benjamin", "Cooper", "Thomas", DateUtil.asDate(LocalDate.of(1997, 9, 12)), "+12025550113")
        );

        UserDto userDto = new UserDto(authId,"benjamincooper13",
                new UserProfileDto("Benjamin", "Thomas", "Cooper", DateUtil.asDate(LocalDate.of(1998, 12, 12)), "+12025550113"));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.saveAndFlush(user)).thenReturn(user);
        when(userMapper.userToUserDto(user)).thenReturn(userDto);

        UserDto updatedUser = userService.updateUserById(userId, userUpdateRequest);

        assertEquals(userDto.authId(), updatedUser.authId());
        assertEquals(userDto.identifier(), updatedUser.identifier());
        assertEquals(userDto.userProfile().firstName(), updatedUser.userProfile().firstName());
        assertEquals(userDto.userProfile().middleName(), updatedUser.userProfile().middleName());
        assertEquals(userDto.userProfile().lastName(), updatedUser.userProfile().lastName());
        assertEquals(userDto.userProfile().birthday(), updatedUser.userProfile().birthday());
        assertEquals(user.getUserProfile().getCellPhoneNumber(), updatedUser.userProfile().cellPhoneNumber());

        verify(userRepository).findById(userId);
        verify(userRepository).saveAndFlush(user);
        verify(userMapper).userToUserDto(user);
    }

    @Test
    void testUpdateUserByIdShouldThrowExceptionWhenUserIsNotExist() {
        String userId = UUID.randomUUID().toString();
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest(
                "charlotteward14", "Charlotte", "", "Ward", DateUtil.asDate(LocalDate.of(1988, 1, 28)), "+12025550114"
        );

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateUserById(userId, userUpdateRequest));

        verify(userRepository).findById(userId);
    }

    @Test
    void testUpdateUserByIdShouldThrowExceptionWhenIsNullable() {
        String userId = UUID.randomUUID().toString();
        String authId = UUID.randomUUID().toString();

        User user = new User(userId, authId, "benjamincooper13",
                new UserProfile("Mia", "Claire", "Richardson", DateUtil.asDate(LocalDate.of(2001, 4, 12)), "+12025550112")
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(Exception.class, () -> userService.updateUserById(userId, null));
        verify(userRepository).findById(userId);
    }

    @Test
    void testDeleteUserByIdShouldRemoveUserWhenUserExists() {
        String userId = UUID.randomUUID().toString();
        String authId = UUID.randomUUID().toString();

        User user = new User(userId, authId, "clairedonovan96", new UserProfile("Claire", "Elise", "Donovan", DateUtil.asDate(LocalDate.of(1998,9,9)), "+12025550196"));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);

        userService.deleteUserById(userId);

        verify(userRepository).delete(user);
    }

    @Test
    void testDeleteUserByIdShouldThrowExceptionWhenUserNotExist() {
        String userId = UUID.randomUUID().toString();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUserById(userId));

        verify(userRepository).findById(userId);
    }

    @Test
    void testDeleteUserByIdShouldThrowExceptionWhenIdIsNullable() {
        String userId = null;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUserById(userId));

        verify(userRepository).findById(userId);
    }

    @Test
    void testDeleteUserByIdShouldThrowExceptionWhenIdIsBlanked() {
        String userId = "";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUserById(userId));

        verify(userRepository).findById(userId);
    }
}