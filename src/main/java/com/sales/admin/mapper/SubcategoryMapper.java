package com.sales.admin.mapper;


import com.sales.admin.dto.SubcategoryDto;
import com.sales.entities.ItemSubCategory;
import com.sales.entities.StoreSubCategory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SubcategoryMapper {
    SubcategoryDto toDto(ItemSubCategory itemSubCategory);
    SubcategoryDto toDto(StoreSubCategory storeSubCategory);
}
