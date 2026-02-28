package com.sales.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AdminItemListDto(
        Long id,
        String name,
        String label,
        Float capacity,
        Float price,
        Float discount,
        String description,
        @JsonProperty("avtars")
        String avatars,
        Float rating,
        Integer totalRatingCount,
        Integer totalReviews,
        Integer totalReportsCount,
        String status,
        Long createdAt,
        String slug,
        String inStock,
        Integer wholesaleId,
        String category,
        String subcategory,
        String createdBy
) {
}




