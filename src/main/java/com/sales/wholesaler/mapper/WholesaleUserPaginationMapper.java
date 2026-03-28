package com.sales.wholesaler.mapper;


import com.sales.entities.UserPagination;
import com.sales.wholesaler.dto.WholesaleUserPagination;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WholesaleUserPaginationMapper {
    WholesaleUserPagination toDto(UserPagination userPagination);
}
