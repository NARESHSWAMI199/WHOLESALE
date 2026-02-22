package com.sales.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ServicePlanRequest extends  SearchFilters{

    Integer months;
    Long price;
    Long discount;
    String planName;
    Integer id;
    String description;
}
