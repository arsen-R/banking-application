package com.arsen.userservice.model.mapper;

import com.arsen.userservice.model.dto.RoleDto;
import com.arsen.userservice.model.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    RoleMapper INSTANCE = Mappers.getMapper(RoleMapper.class);

    RoleDto roleToRoleDto(Role role);
    Role roleDtoToRole(RoleDto roleDto);
}
