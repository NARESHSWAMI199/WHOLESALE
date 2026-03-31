package com.sales.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ServicePlanFilterRequest extends SearchFilters {
    private String name;
    private String slug;
    private String status;
}
