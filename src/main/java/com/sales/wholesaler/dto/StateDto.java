package com.sales.wholesaler.dto;

import lombok.Builder;

@Builder
public record StateDto(
        int id,
        String stateName
) {
}
