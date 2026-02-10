package com.sales.admin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ItemDto(
        Long id,
        String name,
        String label,
        Float capacity,
        Float price,
        Float discount,
        String description,
        String avtars,
        Float rating,
        Integer totalRatingCount,
        Integer totalReviews,
        Integer totalReportsCount,
        String status,
        Long createdAt,
        Long updatedAt,
        String slug,
        String inStock,
        Integer wholesaleId,
        CategoryDto itemCategory,
        SubcategoryDto itemSubCategory,
        UserDto createdBy,
        UserDto updatedBy
) {
}
