package com.sales.wholesaler.dto;


import lombok.Builder;

@Builder
public record WholesaleUserPagination(
        int id,
        WholesalePagination pagination,
        Integer rowsNumber
) {
}
