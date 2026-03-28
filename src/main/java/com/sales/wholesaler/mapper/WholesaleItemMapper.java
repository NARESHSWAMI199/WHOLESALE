package com.sales.wholesaler.mapper;


import com.sales.entities.Item;
import com.sales.wholesaler.dto.WholesaleItemDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring" , uses = {
        WholesaleCategoryMapper.class,
        WholesaleSubcategoryMapper.class,
        WholesaleUserMapper.class
})
public interface WholesaleItemMapper {

    WholesaleItemDto toDto(Item item);

}
