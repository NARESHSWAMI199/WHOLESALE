package com.sales.admin.mapper;


import com.sales.admin.dto.StoreDto;
import com.sales.entities.Store;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {
        SubcategoryMapper.class,
        CategoryMapper.class,
        AddressMapper.class,
        UserMapper.class
})
public interface StoreMapper {
    StoreDto toDto(Store store);
}
