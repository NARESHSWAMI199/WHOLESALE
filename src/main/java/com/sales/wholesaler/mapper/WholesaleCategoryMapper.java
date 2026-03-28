package com.sales.wholesaler.mapper;

import com.sales.entities.ItemCategory;
import com.sales.entities.StoreCategory;
import com.sales.wholesaler.dto.WholesaleCategoryDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WholesaleCategoryMapper {

    WholesaleCategoryDto toDto(ItemCategory itemCategory);
    WholesaleCategoryDto toDto(StoreCategory storeCategory);
}
