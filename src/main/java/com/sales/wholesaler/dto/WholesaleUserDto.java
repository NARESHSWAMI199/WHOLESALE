package com.sales.wholesaler.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record WholesaleUserDto(
        String slug,
        String otp,
        String avatar,
        String username,
        String email,
        String contact,
        String userType,
        String status,
        Integer activePlan,
        Long lastSeen
) {

}
