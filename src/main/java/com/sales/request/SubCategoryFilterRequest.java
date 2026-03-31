package com.sales.request;


import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SubCategoryFilterRequest extends SearchFilters {
    @NotNull
    protected Integer categoryId;
}
