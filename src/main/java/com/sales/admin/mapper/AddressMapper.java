package com.sales.admin.mapper;


import com.sales.admin.dto.AddressDto;
import com.sales.entities.Address;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AddressMapper {
    AddressDto toDto(Address address);
}
