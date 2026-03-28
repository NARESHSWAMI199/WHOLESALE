package com.sales.admin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SubcategoryDto(
        int id,
        String slug,
        Integer categoryId,
        String subcategory,
        String unit,
        String icon
) {
}
