package com.sales.wholesaler.dto;


import lombok.Builder;

@Builder
public record WholesaleServicePlanDto(
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
