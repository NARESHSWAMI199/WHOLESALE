package com.sales.admin.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserDto(
        String slug,
        @JsonIgnore
        String otp,
        String avatar,
        String username,
        @JsonIgnore
        String password,
        String email,
        String contact,
        String userType,
        @JsonIgnore
        String status,
        Integer activePlan,
        Long lastSeen,
        Long createdAt,
        @JsonIgnore
        String fcmToken
) {

}
