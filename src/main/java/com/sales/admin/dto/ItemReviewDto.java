package com.sales.admin.dto;


import lombok.Builder;

@Builder
public record ItemReviewDto(
        long id,
        Float rating,
        String slug,
        UserDto user,
        Long likes,
        Integer parentId,
        String message,
        String createdAt,
        Integer repliesCount,
        String avatar
) {
}
