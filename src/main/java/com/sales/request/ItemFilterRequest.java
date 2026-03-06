package com.sales.request;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ItemFilterRequest extends SearchFilters {
    private String slug;
    private String status;
    private int storeId;
    private String inStock;
    private String label;
}
