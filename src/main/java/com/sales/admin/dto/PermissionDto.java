package com.sales.admin.dto;

public record PermissionDto(
        int id,
        String permissionFor,
        String displayName
) {
}