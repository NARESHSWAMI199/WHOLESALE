package com.sales.chats.mapper;

import com.sales.chats.dto.UserDto;
import com.sales.entities.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapperForChat {
    UserDto toDto(User user);
}
