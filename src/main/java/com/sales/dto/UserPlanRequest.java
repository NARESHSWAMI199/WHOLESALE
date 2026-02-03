package com.sales.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class UserPlanRequest extends SearchFilters {
    Long createdFromDate;
    Long createdToDate;
    Long expiredFromDate;
    Long expiredToDate;
}



