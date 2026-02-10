package com.sales.admin.dto;

import com.sales.entities.ReportCategory;
import lombok.Builder;

@Builder
public record ItemReportDto(
        long id,
        Long itemId,
        ReportCategory reportCategory,
        UserDto user,
        String message,
        Long createdAt,
        Long updatedAt
) {
}
