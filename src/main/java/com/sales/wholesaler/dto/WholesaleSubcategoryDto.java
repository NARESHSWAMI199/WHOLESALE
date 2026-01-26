package com.sales.wholesaler.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record WholesaleSubcategoryDto(
        int id,
        String slug,
        Integer categoryId,
        String subcategory,
        String unit,
        String icon
) {
}
