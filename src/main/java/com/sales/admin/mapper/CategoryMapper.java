package com.sales.admin.mapper;

import com.sales.admin.dto.CategoryDto;
import com.sales.entities.ItemCategory;
import com.sales.entities.StoreCategory;
import com.sales.wholesaler.dto.WholesaleCategoryDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryDto toDto(ItemCategory itemCategory);
    CategoryDto toDto(StoreCategory storeCategory);
}
