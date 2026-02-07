package com.sales.wholesaler.dto;

import lombok.Builder;

@Builder
public record WholesalerFuturePlanDto(
        long id,
        String slug,
        WholesaleServicePlanDto servicePlan,
        Long createdAt,
        String status
) {


}
