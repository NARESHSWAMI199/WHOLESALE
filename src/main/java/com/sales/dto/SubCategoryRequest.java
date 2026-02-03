package com.sales.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubCategoryRequest {
    Integer id;
    Integer categoryId;
    String subcategory;
    String icon;
    String unit;
}
