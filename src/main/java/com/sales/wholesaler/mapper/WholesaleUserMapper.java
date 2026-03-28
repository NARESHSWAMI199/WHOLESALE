package com.sales.wholesaler.mapper;


import com.sales.entities.User;
import com.sales.wholesaler.dto.WholesaleUserDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WholesaleUserMapper {
    WholesaleUserDto toDto(User user);
}
