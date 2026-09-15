package com.arsen.userservice.service;

import com.arsen.userservice.exception.CreateUserException;
import com.arsen.userservice.exception.InvalidPasswordException;
import com.arsen.userservice.exception.UserNotFoundException;
import com.arsen.userservice.model.dto.UserDto;
import com.arsen.userservice.model.entity.User;
import com.arsen.userservice.model.entity.UserProfile;
import com.arsen.userservice.model.enums.UserStatus;
import com.arsen.userservice.model.mapper.RoleMapper;
import com.arsen.userservice.model.mapper.UserMapper;
import com.arsen.userservice.model.request.ChangeUserStatusRequest;
import com.arsen.userservice.model.request.CreateUserRequest;
import com.arsen.userservice.model.request.PasswordUpdateRequest;
import com.arsen.userservice.model.request.UserUpdateRequest;
import com.arsen.userservice.model.response.PageResponse;
import com.arsen.userservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final RoleMapper roleMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, BCryptPasswordEncoder bCryptPasswordEncoder, RoleMapper roleMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.roleMapper = roleMapper;
    }

    @Override
    public PageResponse<UserDto> findAllUsers(Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<User> userPages = userRepository.findAll(pageable);
        List<UserDto> userDtos = userPages.stream().map(userMapper::userToUserDto).toList();
        return PageResponse.<UserDto>builder().content(userDtos)
                .page(page)
                .pageSize(pageSize)
                .totalElements(userPages.getTotalElements())
                .totalPages(userPages.getTotalPages())
                .build();
    }

    @Override
    public UserDto findUserById(String userId) {
        return userRepository.findById(userId).map(userMapper::userToUserDto).orElseThrow(() ->
                new UserNotFoundException("User not found! Check your id is correct or not blank!"));
    }

    @Override
    public UserDto findUserByEmail(String email) {
        return userRepository.findByEmail(email).map(userMapper::userToUserDto).orElseThrow(() ->
                new UserNotFoundException("User not found!"));
    }

    @Override
    @Transactional
    public UserDto createUser(CreateUserRequest createUserRequest) {
        if (userRepository.existsByEmail(createUserRequest.email())) {
            throw new CreateUserException("Email already exists!");
        }
        UserProfile userProfile = new UserProfile(
                createUserRequest.firstName(),
                createUserRequest.middleName(),
                createUserRequest.lastName(),
                createUserRequest.birthday(),
                createUserRequest.cellPhoneNumber()
        );

        User newUser = new User(
                createUserRequest.username(),
                createUserRequest.email(),
                bCryptPasswordEncoder.encode(createUserRequest.password()),
                UserStatus.ACTIVE,
                userProfile,
                createUserRequest.roles().stream().map(roleMapper::roleDtoToRole).collect(Collectors.toSet())
        );

        User savedUser = userRepository.saveAndFlush(newUser);
        return userMapper.userToUserDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto updateUserById(String userId, UserUpdateRequest userUpdateRequest) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found!"));

        user.setUsername(userUpdateRequest.username());
        user.setEmail(userUpdateRequest.email());
        user.getUserProfile().setFirstName(userUpdateRequest.firstName());
        user.getUserProfile().setMiddleName(userUpdateRequest.middleName());
        user.getUserProfile().setLastName(userUpdateRequest.lastName());
        user.getUserProfile().setCellPhoneNumber(userUpdateRequest.cellPhoneNumber());
        user.getUserProfile().setBirthday(userUpdateRequest.birthday());
        user.setUpdateTime(Date.from(Instant.now()));

        User savedUser = userRepository.saveAndFlush(user);
        return userMapper.userToUserDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto changeCurrentPasswordByUserId(String userId, PasswordUpdateRequest passwordUpdateRequest) {
        User user  = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found!"));
        if(!bCryptPasswordEncoder.matches(passwordUpdateRequest.currentPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Current password is incorrect!");
        }
        if (passwordUpdateRequest.currentPassword().length() < 8 || passwordUpdateRequest.newPassword().length() < 8) {
            throw new IllegalArgumentException("Password length is less than 8 characters!");
        }
        user.setPassword(bCryptPasswordEncoder.encode(passwordUpdateRequest.newPassword()));
        User savedUser = userRepository.saveAndFlush(user);
        return userMapper.userToUserDto(savedUser);
    }

    @Override
    @Transactional
    public void deleteUserById(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found!"));
        user.setStatus(UserStatus.DELETED);
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public UserDto changeUserStatus(String userId, ChangeUserStatusRequest changeUserStatusRequest) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found!"));
        user.setStatus(changeUserStatusRequest.userStatus());
        User savedUser = userRepository.saveAndFlush(user);
        return userMapper.userToUserDto(savedUser);
    }
}
