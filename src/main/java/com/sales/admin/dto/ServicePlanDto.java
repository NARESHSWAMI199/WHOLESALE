package com.sales.admin.dto;


import lombok.Builder;

@Builder
public record ServicePlanDto(
        String slug,
        String name,
        Long price,
        Long discount,
        String status,
        String icon,
        Integer months,
        String description
) {
}
