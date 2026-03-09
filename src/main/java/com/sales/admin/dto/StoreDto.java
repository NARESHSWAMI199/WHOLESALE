package com.sales.admin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record StoreDto(
        String slug,
        String storeName,
        String avtar,
        String email,
        String phone,
        String description,
        Float rating,
        UserDto user,
        String status,
        String isDeleted,
        Long createdAt,
        AddressDto address,
        Integer totalStoreItems,
        CategoryDto storeCategory,
        SubcategoryDto storeSubCategory,
        Long updatedAt
) {
}
