package com.sales.chats.mapper;


import com.sales.chats.dto.ChatUserDto;
import com.sales.entities.ChatUser;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChatUserMapper {
    ChatUserDto toDto(ChatUser chatUser);
}
