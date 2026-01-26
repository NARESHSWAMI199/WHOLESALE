package com.sales.wholesaler.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record WholesaleStoreDto(
        String slug,
        String storeName,
        String avtar,
        String email,
        String phone,
        String description,
        Float rating,
        String status,
        String isDeleted,
        Long createdAt,
        WholesaleAddressDto address,
        Integer totalStoreItems,
        WholesaleCategoryDto storeCategory,
        WholesaleSubcategoryDto storeSubCategory
) {
}
