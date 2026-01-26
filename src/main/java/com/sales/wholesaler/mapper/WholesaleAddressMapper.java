package com.sales.wholesaler.mapper;


import com.sales.entities.Address;
import com.sales.wholesaler.dto.WholesaleAddressDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WholesaleAddressMapper {
    WholesaleAddressDto toDto(Address address);
}
