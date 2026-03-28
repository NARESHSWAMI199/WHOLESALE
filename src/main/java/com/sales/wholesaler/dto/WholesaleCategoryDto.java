package com.sales.wholesaler.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record WholesaleCategoryDto(
        int id,
        String slug,
        String category,
        String icon
) {
}
