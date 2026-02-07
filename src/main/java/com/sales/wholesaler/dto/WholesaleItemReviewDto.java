package com.sales.wholesaler.dto;


import lombok.Builder;

@Builder
public record WholesaleItemReviewDto(
        long id,
        Float rating,
        String slug,
        WholesaleUserDto user,
        Long likes,
        Integer parentId,
        String message,
        String createdAt,
        Integer repliesCount,
        String avatar
) {
}
