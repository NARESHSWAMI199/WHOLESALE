package com.sales.admin.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserDto(
        String slug,
        String otp,
        String avatar,
        String username,
        String password,
        String email,
        String contact,
        String userType,
        String status,
        Integer activePlan,
        Long lastSeen
) {

}
