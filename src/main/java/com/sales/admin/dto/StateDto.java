package com.sales.admin.dto;

import lombok.Builder;

@Builder
public record StateDto(
    int id,
    String stateName
) {
}
