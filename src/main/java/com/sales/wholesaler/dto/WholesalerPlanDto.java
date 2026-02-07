package com.sales.wholesaler.dto;

import lombok.Builder;

@Builder
public record WholesalerPlanDto(
        int id,
        String slug,
        Integer userId,
        WholesaleServicePlanDto servicePlan,
        Long createdAt,
        Long expiryDate,
        boolean isExpired
) {
}
