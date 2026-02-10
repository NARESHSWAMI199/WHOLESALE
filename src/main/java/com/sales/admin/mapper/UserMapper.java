package com.sales.admin.mapper;


import com.sales.admin.dto.UserDto;
import com.sales.entities.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);
}
