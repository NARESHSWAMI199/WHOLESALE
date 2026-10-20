package com.sales.admin.dto;


import lombok.Builder;

@Builder(toBuilder = true)
public record UploadInitDto(
        String uploadId,
        boolean isCompleted
) {
}
