package com.sales.admin.dto;


import lombok.Builder;

@Builder
public record UserPagination(
        int id,
        PaginationDto pagination,
        Integer rowsNumber
) {
}
