package com.sales.admin.dto;

import lombok.Builder;

@Builder
public record PlanDto(
        int id,
        String slug,
        Integer userId,
        ServicePlanDto servicePlan,
        Long createdAt,
        Long expiryDate,
        boolean isExpired
) {
}
