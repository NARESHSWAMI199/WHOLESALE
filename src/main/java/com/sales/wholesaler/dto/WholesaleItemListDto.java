package com.sales.wholesaler.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;


@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record WholesaleItemListDto(
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
        String slug,
        String inStock,
        Integer wholesaleId,
        String category,
        String subcategory,
        String createdBy
) {
}




