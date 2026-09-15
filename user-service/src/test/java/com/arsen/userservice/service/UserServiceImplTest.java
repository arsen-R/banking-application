package com.arsen.userservice.service;

import com.arsen.userservice.exception.CreateUserException;
import com.arsen.userservice.exception.InvalidPasswordException;
import com.arsen.userservice.exception.UserNotFoundException;
import com.arsen.userservice.model.dto.RoleDto;
import com.arsen.userservice.model.dto.UserDto;
import com.arsen.userservice.model.dto.UserProfileDto;
import com.arsen.userservice.model.entity.Role;
import com.arsen.userservice.model.entity.User;
import com.arsen.userservice.model.entity.UserProfile;
import com.arsen.userservice.model.enums.RoleName;
import com.arsen.userservice.model.enums.UserStatus;
import com.arsen.userservice.model.mapper.RoleMapper;
import com.arsen.userservice.model.mapper.UserMapper;
import com.arsen.userservice.model.request.ChangeUserStatusRequest;
import com.arsen.userservice.model.request.CreateUserRequest;
import com.arsen.userservice.model.request.PasswordUpdateRequest;
import com.arsen.userservice.model.request.UserUpdateRequest;
import com.arsen.userservice.model.response.PageResponse;
import com.arsen.userservice.repository.RoleRepository;
import com.arsen.userservice.repository.UserProfileRepository;
import com.arsen.userservice.repository.UserRepository;
import com.arsen.userservice.util.DateUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
    private UserProfileRepository userProfileRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Mock
    private RoleMapper roleMapper;

    private Role role;

    @BeforeEach
    void setUp() {
        role = new Role(RoleName.CUSTOMER);
        roleRepository.save(role);
    }

    @Test
    void testFindAllUsersShouldReturnAllUsersWhenUsersAreFound() {
        int page = 0;
        int pageSize = 3;
        List<User> users = List.of(
                new User("weichen", "weichen1989@example.com", bCryptPasswordEncoder.encode("Qu1ckFox!22"), UserStatus.ACTIVE),
                new User("mrodriguez", "maria.rodriguez@example.com", bCryptPasswordEncoder.encode("Zeph7r@Lime"), UserStatus.ACTIVE),
                new User("srossi", "sofia.rossi@example.com", bCryptPasswordEncoder.encode("P0lar!Bear4"), UserStatus.ACTIVE)
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
        User user = new User("91", "morgan.vera", "morgan.vera1993@example.com", "GraniteW0lf#", UserStatus.ACTIVE,
                new UserProfile("Morgan", "Vera", DateUtil.asDate(LocalDate.of(2002, 1, 12)), "+12125550124"),
                Set.of(role)
        );

        UserDto userDto = new UserDto("morgan.vera", "morgan.vera1993@example.com", UserStatus.ACTIVE,
                new UserProfileDto("Morgan", "Vera", DateUtil.asDate(LocalDate.of(2002, 1, 12)), "+12125550124"),
                Set.of(new RoleDto(RoleName.CUSTOMER))
        );

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userMapper.userToUserDto(user)).thenReturn(userDto);

        UserDto foundUser = userService.findUserById(user.getId());

        assertEquals(userDto.email(), foundUser.email());
        assertEquals(userDto.username(), foundUser.username());
        assertEquals(userDto.userStatus(), foundUser.userStatus());

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
        User user = new User("liamcarter01", "liam.carter01@example.com", "GraniteW0lf#", UserStatus.ACTIVE,
                new UserProfile("Liam", "James", "Carter", DateUtil.asDate(LocalDate.of(1994, 9, 25)), "+12025550101"),
                Set.of(role)
        );

        UserDto userDto = new UserDto("liamcarter01", "liam.carter01@example.com", UserStatus.ACTIVE,
                new UserProfileDto("Liam", "James", "Carter", DateUtil.asDate(LocalDate.of(1994, 9, 25)), "+12025550101"),
                Set.of(new RoleDto(RoleName.CUSTOMER))
        );

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userMapper.userToUserDto(user)).thenReturn(userDto);

        UserDto foundUser = userService.findUserByEmail(user.getEmail());

        assertThat(foundUser).isEqualTo(userDto);
        assertEquals(userDto.email(), foundUser.email());
        assertEquals(userDto.username(), foundUser.username());
        assertEquals(userDto.userStatus(), foundUser.userStatus());

        verify(userRepository).findByEmail(user.getEmail());
        verify(userMapper).userToUserDto(user);
    }

    @Test
    void testFindUserByEmailShouldThrowExceptionWhenUserIsNotFound() {
        String email = "sophie.grant84@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.findUserByEmail(email));

        verify(userRepository).findByEmail(email);
    }

    @Test
    void testFindUserByEmailShouldThrowExceptionWhenEmailIsBlank() {
        when(userRepository.findByEmail("")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.findUserByEmail(""));

        verify(userRepository).findByEmail("");
    }

    @Test
    void testFindUserByEmailShouldThrowExceptionWhenEmailIsNull() {
        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.findUserByEmail(null));

        verify(userRepository).findByEmail(null);
    }

    @Test
    void testFindUserByEmailShouldThrowExceptionWhenEmailIsNotValid() {
        String email = "emma.mitchell02example";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUserByEmail(email)).isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findByEmail(email);
    }

    @Test
    void testCreateUserShouldReturnUserWhenUserIsNotExist() {
        RoleDto rolesDto = new RoleDto(RoleName.CUSTOMER);

        CreateUserRequest createUserRequest = new CreateUserRequest("emilyramirez24", "emily.ramirez24@example.com", "Test@Emily24!", Set.of(rolesDto), "Emily", "Sophia", "Ramirez", DateUtil.asDate(LocalDate.of(1994, 12, 2)), "+12025550124");
        String encodedPassword = "$2a$10$encodedPasswordPlaceholder";

        when(bCryptPasswordEncoder.encode(createUserRequest.password())).thenReturn(encodedPassword);

        UserProfile userProfile = new UserProfile("Emily", "Sophia", "Ramirez", DateUtil.asDate(LocalDate.of(1994, 12, 2)), "+12025550124");
        User user = new User("emilyramirez24", "emily.ramirez24@example.com", encodedPassword, UserStatus.ACTIVE, userProfile, Set.of(role));

        UserProfileDto userProfileDto = new UserProfileDto("Emily", "Sophia", "Ramirez", DateUtil.asDate(LocalDate.of(1994, 12, 2)), "+12025550124");
        UserDto userDto = new UserDto("emilyramirez24", "emily.ramirez24@example.com", UserStatus.ACTIVE, userProfileDto, Set.of(rolesDto));

        when(userRepository.existsByEmail(createUserRequest.email())).thenReturn(false);
        when(roleMapper.roleDtoToRole(rolesDto)).thenReturn(role);
        when(userRepository.saveAndFlush(user)).thenReturn(user);
        when(userMapper.userToUserDto(user)).thenReturn(userDto);

        UserDto createdUserDto = userService.createUser(createUserRequest);

        assertEquals(userDto.email(), createdUserDto.email());
        assertEquals(userDto.username(), createdUserDto.username());
        assertEquals(userDto.userProfile().firstName(), createdUserDto.userProfile().firstName());
        assertEquals(userDto.userProfile().middleName(), createdUserDto.userProfile().middleName());
        assertEquals(userDto.userProfile().lastName(), createdUserDto.userProfile().lastName());
        assertEquals(userDto.userProfile().birthday(), createdUserDto.userProfile().birthday());
        assertEquals(userDto.userProfile().cellPhoneNumber(), createdUserDto.userProfile().cellPhoneNumber());

        verify(userRepository).existsByEmail(createUserRequest.email());
        verify(bCryptPasswordEncoder).encode(createUserRequest.password());
        verify(userRepository).saveAndFlush(user);
        verify(userMapper).userToUserDto(user);
        verify(roleMapper).roleDtoToRole(rolesDto);
    }

    @Test
    void testCreateUserShouldThrowExceptionWhenUserIsExist() {
        CreateUserRequest createUserRequest = new CreateUserRequest("lucasturner09", "lucas.turner09@example.com", "Test@Lucas09!", Set.of(new RoleDto(RoleName.CUSTOMER)), "Lucas", "William", DateUtil.asDate(LocalDate.of(1984, 2, 25)), "+12025550109");

        when(userRepository.existsByEmail(createUserRequest.email())).thenReturn(true);

        assertThrows(CreateUserException.class, () -> userService.createUser(createUserRequest));

        verify(userRepository).existsByEmail(createUserRequest.email());
    }

    @Test
    void testCreateUserShouldThrowExceptionWhenUserIsNull() {
        assertThrows(NullPointerException.class, () -> userService.createUser(null));
    }

    @Test
    void testUpdateUserByIdShouldReturnUserWhenUserIsUpdated() {
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest(
                "benjamincooper13", "benjamin.cooper13@example.com", "Benjamin", "Thomas", "Cooper", DateUtil.asDate(LocalDate.of(1998, 12, 12)), "+12025550113"
        );
        String userId = UUID.randomUUID().toString();

        User user = new User(userId, "benjamincooper13", "benjamin.cooper14@example.com", "Test@Benjamin13!", UserStatus.ACTIVE,
                new UserProfile("Benjamin", "Cooper", "Thomas", DateUtil.asDate(LocalDate.of(1997, 9, 12)), "+12025550113"),
                Set.of(role)
        );

        UserDto userDto = new UserDto("benjamincooper13", "benjamin.cooper13@example.com", UserStatus.ACTIVE,
                new UserProfileDto("Benjamin", "Thomas", "Cooper", DateUtil.asDate(LocalDate.of(1998, 12, 12)), "+12025550113"), Set.of(new RoleDto(RoleName.CUSTOMER)));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.saveAndFlush(user)).thenReturn(user);
        when(userMapper.userToUserDto(user)).thenReturn(userDto);

        UserDto updatedUser = userService.updateUserById(userId, userUpdateRequest);

        assertEquals(userDto.email(), updatedUser.email());
        assertEquals(userDto.username(), updatedUser.username());
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
                "charlotteward14", "charlotte.ward14@example.com", "Charlotte", "", "Ward", DateUtil.asDate(LocalDate.of(1988, 1, 28)), "+12025550114"
        );

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateUserById(userId, userUpdateRequest));

        verify(userRepository).findById(userId);
    }

    @Test
    void testUpdateUserByIdShouldThrowExceptionWhenIsNullable() {
        String userId = UUID.randomUUID().toString();
        User user = new User(userId, "benjamincooper13", "mia.richardson12@example.com", "TTest@Mia12!", UserStatus.ACTIVE,
                new UserProfile("Mia", "Claire", "Richardson", DateUtil.asDate(LocalDate.of(2001, 4, 12)), "+12025550112"),
                Set.of(role)
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(Exception.class, () -> userService.updateUserById(userId, null));
        verify(userRepository).findById(userId);
    }

    @Test
    void testUpdatePasswordShouldReturnUserWhenPasswordIsUpdated() {
        String userId = UUID.randomUUID().toString();
        String currentPassword = "NOT-A-REAL-PASSWORD-0009";
        String currentEncodedPassword = bCryptPasswordEncoder.encode(currentPassword);
        String newPassword = "Test@Isaac49!";

        PasswordUpdateRequest passwordUpdateRequest = new PasswordUpdateRequest(currentPassword, newPassword);
        User user = new User(userId, "isaachunter49", "isaac.hunter49@example.com", currentEncodedPassword, UserStatus.ACTIVE);
        UserDto userDto = new UserDto("isaachunter49", "isaac.hunter49@example.com", UserStatus.ACTIVE);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bCryptPasswordEncoder.matches(passwordUpdateRequest.currentPassword(),currentEncodedPassword)).thenReturn(true);
        when(bCryptPasswordEncoder.encode(newPassword)).thenReturn(newPassword); // the missing stub
        when(userRepository.saveAndFlush(user)).thenReturn(user);
        when(userMapper.userToUserDto(user)).thenReturn(userDto);

        UserDto updatedUser = userService.changeCurrentPasswordByUserId(userId, passwordUpdateRequest);

        assertEquals(userDto, updatedUser);
        assertEquals(newPassword, user.getPassword());

        verify(userRepository).findById(userId);
        verify(userRepository).saveAndFlush(user);
        verify(userMapper).userToUserDto(user);
        verify(bCryptPasswordEncoder).matches(passwordUpdateRequest.currentPassword(), currentEncodedPassword);
        verify(bCryptPasswordEncoder).encode(newPassword);
    }

    @Test
    void testUpdatePasswordShouldThrowExceptionWhenUserNotExist() {
        String userId = UUID.randomUUID().toString();
        PasswordUpdateRequest passwordUpdateRequest = new PasswordUpdateRequest("Test@Andrew39!", "Test@Victoria50!");
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.changeCurrentPasswordByUserId(userId, passwordUpdateRequest));
        verify(userRepository).findById(userId);
    }

    @Test
    void testUpdatePasswordShouldThrowExceptionWhenPasswordIsNullable() {
        String userId = UUID.randomUUID().toString();
        User user = new User(userId, "leojennings59", "leo.jennings59@example.com", bCryptPasswordEncoder.encode("Test@Leo59!"), UserStatus.ACTIVE);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(NullPointerException.class, () -> userService.changeCurrentPasswordByUserId(userId, null));

        verify(userRepository).findById(userId);
    }

    @Test
    void testUpdatePasswordShouldThrowExceptionWhenCurrentPasswordIsIncorrect() {
        String userId = UUID.randomUUID().toString();
        String currentPassword = "NOT-A-REAL-PASSWORD-0009";

        PasswordUpdateRequest passwordUpdateRequest = new PasswordUpdateRequest(currentPassword,"Test@Jeremiah87!" );
        User user = new User(userId, "jeremiahosborne87", "jeremiah.osborne87@example.com", bCryptPasswordEncoder.encode("Test@Bella86!"), UserStatus.ACTIVE);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bCryptPasswordEncoder.matches(passwordUpdateRequest.currentPassword(), user.getPassword())).thenReturn(false);

        assertThrows(InvalidPasswordException.class, () -> userService.changeCurrentPasswordByUserId(userId, passwordUpdateRequest));

        verify(userRepository).findById(userId);
        verify(bCryptPasswordEncoder).matches(passwordUpdateRequest.currentPassword(), user.getPassword());
    }

    @Test
    void testUpdatePasswordShouldThrowExceptionWhenCurrentPasswordsHasLessLengthCharacters() {
        String userId = UUID.randomUUID().toString();
        PasswordUpdateRequest passwordUpdateRequest = new PasswordUpdateRequest("Test@Vi","Test@Natalie94!" );
        User user = new User(userId, "nataliequinn94", "natalie.quinn94@example.com", bCryptPasswordEncoder.encode("Test@Natalie100#"), UserStatus.ACTIVE);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bCryptPasswordEncoder.matches(passwordUpdateRequest.currentPassword(), user.getPassword())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.changeCurrentPasswordByUserId(userId, passwordUpdateRequest));

        verify(userRepository).findById(userId);
        verify(bCryptPasswordEncoder).matches(passwordUpdateRequest.currentPassword(), user.getPassword());
    }

    @Test
    void testUpdatePasswordShouldThrowExceptionWhenNewPasswordsHasLessLengthCharacters() {
        String userId = UUID.randomUUID().toString();
        PasswordUpdateRequest passwordUpdateRequest = new PasswordUpdateRequest("Test@Victoria100!","Test@J" );
        User user = new User(userId, "victoriaharper100", "victoria.harper100@example.com", bCryptPasswordEncoder.encode("Test@Victoria100!"), UserStatus.ACTIVE);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bCryptPasswordEncoder.matches(passwordUpdateRequest.currentPassword(), user.getPassword())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.changeCurrentPasswordByUserId(userId, passwordUpdateRequest));

        verify(userRepository).findById(userId);
        verify(bCryptPasswordEncoder).matches(passwordUpdateRequest.currentPassword(), user.getPassword());
    }

    @Test
    void testDeleteUserByIdShouldRemoveUserWhenUserExists() {
        String userId = UUID.randomUUID().toString();
        User user = new User(userId, "clairedonovan96", "claire.donovan96@example.com", "Test@Claire96!", UserStatus.ACTIVE);

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

    @Test
    void testChangeUserStatusByIdShouldReturnUserWhenUserExists() {
        String userId = UUID.randomUUID().toString();
        ChangeUserStatusRequest changeUserStatusRequest = new ChangeUserStatusRequest(UserStatus.SUSPENDED);
        User user = new User(userId, "kevinmonroe97", "kevin.monroe97@example.com", bCryptPasswordEncoder.encode("Test@Kevin97!"), UserStatus.ACTIVE);
        UserDto userDto = new UserDto("kevinmonroe97", "kevin.monroe97@example.com", UserStatus.SUSPENDED);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.saveAndFlush(user)).thenReturn(user);
        when(userMapper.userToUserDto(user)).thenReturn(userDto);

        UserDto savedUser = userService.changeUserStatus(userId, changeUserStatusRequest);

        assertEquals(userDto.userStatus(), savedUser.userStatus());
        assertEquals(user.getStatus(), savedUser.userStatus());

        verify(userRepository).findById(userId);
        verify(userRepository).saveAndFlush(user);
        verify(userMapper).userToUserDto(user);
    }

    @Test
    void testChangeUserStatusByIdShouldThrowExceptionWhenUserNotExist() {
        String userId = UUID.randomUUID().toString();
        ChangeUserStatusRequest changeUserStatusRequest = new ChangeUserStatusRequest(UserStatus.INACTIVE);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.changeUserStatus(userId, changeUserStatusRequest));

        verify(userRepository).findById(userId);
    }

    @Test
    void testChangeUserStatusByIdShouldThrowExceptionWhenIdIsBlank() {
        String userId = "";
        ChangeUserStatusRequest changeUserStatusRequest = new ChangeUserStatusRequest(UserStatus.DELETED);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.changeUserStatus(userId, changeUserStatusRequest));

        verify(userRepository).findById(userId);
    }

    @Test
    void testChangeUserStatusByIdShouldThrowExceptionWhenIdIsNull() {
        String userId = null;
        ChangeUserStatusRequest changeUserStatusRequest = new ChangeUserStatusRequest(UserStatus.DELETED);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.changeUserStatus(userId, changeUserStatusRequest));

        verify(userRepository).findById(userId);
    }
}