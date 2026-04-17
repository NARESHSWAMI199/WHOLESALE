package com.sales.chats.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
public record UserDto(
        String slug,
        String otp,
        String avatar,
        String username,
        String email,
        String contact,
        String userType,
        String status,
        Integer activePlan,
        Long lastSeen,
        Long updatedAt,
        Long createdAt,
        Integer chatNotification,
        String avatarUrl
) {

}
