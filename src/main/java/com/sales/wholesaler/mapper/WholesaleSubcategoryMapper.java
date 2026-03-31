package com.sales.wholesaler.mapper;


import com.sales.entities.ItemSubCategory;
import com.sales.entities.StoreSubCategory;
import com.sales.wholesaler.dto.WholesaleSubcategoryDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WholesaleSubcategoryMapper {
    WholesaleSubcategoryDto toDto(ItemSubCategory itemSubCategory);

    WholesaleSubcategoryDto toDto(StoreSubCategory storeSubCategory);
}
