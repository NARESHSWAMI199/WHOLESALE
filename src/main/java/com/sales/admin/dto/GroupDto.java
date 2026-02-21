package com.sales.admin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record GroupDto(
        int id,
        String name,
        String slug,
        Long createdAt,
        Long updatedAt
) {
}
