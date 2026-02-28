package com.sales.request;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemSearchFields extends SearchFilters{
    String inStock;
    String label;
}
