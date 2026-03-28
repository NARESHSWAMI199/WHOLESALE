package com.sales.admin.dto;

import lombok.Builder;

@Builder
public record FuturePlanDto(
        long id,
        String slug,
        ServicePlanDto servicePlan,
        Long createdAt,
        String status
) {


}
