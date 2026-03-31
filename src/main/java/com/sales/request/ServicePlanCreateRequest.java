package com.sales.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ServicePlanCreateRequest {
    private String name;
    private String slug;
    private String status;
    private Integer months;
    private Long price;
    private Long discount;
    private String planName;
    private Integer id;
    private String description;
}
