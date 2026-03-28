package com.sales.wholesaler.mapper;


import com.sales.entities.Store;
import com.sales.wholesaler.dto.WholesaleStoreDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring",uses = {
        WholesaleSubcategoryMapper.class,
        WholesaleCategoryMapper.class,
        WholesaleAddressMapper.class,
        WholesaleUserMapper.class
})
public interface WholesaleStoreMapper {
    WholesaleStoreDto toDto(Store store);
}
