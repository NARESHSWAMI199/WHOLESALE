package com.sales.chats.dto;

import lombok.Builder;

@Builder
public record ContactDto(
        int id,
        Integer userId,
        UserDto contactUser
) {
}
