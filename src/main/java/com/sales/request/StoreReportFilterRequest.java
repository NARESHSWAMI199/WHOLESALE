package com.sales.request;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class StoreReportFilterRequest extends SearchFilters {
    private Integer storeId;
}
