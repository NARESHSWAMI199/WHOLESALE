package com.sales.wholesaler.dto;


import com.sales.entities.Pagination;
import lombok.Builder;

@Builder
public record WholesaleUserPagination(
        int id,
        WholesalePagination pagination,
        Integer rowsNumber
) {
}
