package com.arsen.userservice.service;

import com.arsen.userservice.exception.CreateUserException;
import com.arsen.userservice.exception.UserNotFoundException;
import com.arsen.userservice.model.dto.UserDto;
import com.arsen.userservice.model.entity.User;
import com.arsen.userservice.model.entity.UserProfile;
import com.arsen.userservice.model.mapper.UserMapper;
import com.arsen.userservice.model.request.CreateUserRequest;
import com.arsen.userservice.model.request.UserUpdateRequest;
import com.arsen.userservice.model.response.PageResponse;
import com.arsen.userservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
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
    public UserDto findUserByIdentifier(String identifier) {
        return userRepository.findByIdentifier(identifier).map(userMapper::userToUserDto).orElseThrow(() ->
                new UserNotFoundException("User not found!"));
    }

    @Override
    @Transactional
    public UserDto createUser(CreateUserRequest createUserRequest) {
        if (userRepository.existsByAuthId(createUserRequest.authId())) {
            throw new CreateUserException("User already exists!");
        }
        UserProfile userProfile = new UserProfile(
                createUserRequest.firstName(),
                createUserRequest.middleName(),
                createUserRequest.lastName(),
                createUserRequest.birthday(),
                createUserRequest.cellPhoneNumber()
        );

        User savedUser = userRepository.saveAndFlush(new User(createUserRequest.authId(), createUserRequest.username(), userProfile));
        return userMapper.userToUserDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto updateUserById(String userId, UserUpdateRequest userUpdateRequest) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found!"));

        user.setIdentifier(userUpdateRequest.username());
        user.getUserProfile().setFirstName(userUpdateRequest.firstName());
        user.getUserProfile().setMiddleName(userUpdateRequest.middleName());
        user.getUserProfile().setLastName(userUpdateRequest.lastName());
        user.getUserProfile().setCellPhoneNumber(userUpdateRequest.cellPhoneNumber());
        user.getUserProfile().setBirthday(userUpdateRequest.birthday());
        user.setUpdatedAt(Date.from(Instant.now()));

        User savedUser = userRepository.saveAndFlush(user);
        return userMapper.userToUserDto(savedUser);
    }

    @Override
    @Transactional
    public void deleteUserById(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found!"));
        userRepository.delete(user);
    }
}
