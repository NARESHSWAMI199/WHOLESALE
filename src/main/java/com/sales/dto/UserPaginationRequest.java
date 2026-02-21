package com.sales.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder
public class UserPaginationRequest {
    Integer paginationId;
    Integer userId;
    Integer rowsNumber;
}
