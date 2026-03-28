package com.sales.wholesaler.dto;


import lombok.Builder;

@Builder
public record WholesalePagination(
        int id,
        String fieldFor
) {
}
