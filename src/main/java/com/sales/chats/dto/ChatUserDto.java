package com.sales.chats.dto;

import lombok.Builder;

@Builder
public record ChatUserDto(
    int id,
    Integer userId,
    UserDto chatUser,
    String senderAcceptStatus
) {
}
