package com.sales.admin.mapper;


import com.sales.admin.dto.ItemDto;
import com.sales.entities.Item;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {
        CategoryMapper.class,
        SubcategoryMapper.class,
        UserMapper.class
})
public interface ItemMapper {

    ItemDto toDto(Item item);

}
