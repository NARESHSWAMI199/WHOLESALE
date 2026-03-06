package com.sales.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubCategoryRequest {
    private Integer id;
    @NotNull
    private Integer categoryId;
    @NotNull
    @NotBlank
    private String subcategory;
    @NotNull
    @NotBlank
    private String icon;
    @NotNull
    @NotBlank
    private String unit;
}
