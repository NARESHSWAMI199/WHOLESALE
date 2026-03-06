package com.sales.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryRequest {
    Integer id;
    @NotNull
    @NotBlank
    String category;
    String icon;
}
