package com.sales.admin.dto;


import lombok.Builder;

@Builder
public record PaginationDto(
        int id,
        String fieldFor
) {
}
