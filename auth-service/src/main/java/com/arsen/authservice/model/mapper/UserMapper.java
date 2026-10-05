package com.arsen.authservice.model.mapper;

import com.arsen.authservice.model.dto.UserDto;
import com.arsen.authservice.model.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto userToUserDto(User user);
    User userDtoToUser(UserDto userDto);
}
