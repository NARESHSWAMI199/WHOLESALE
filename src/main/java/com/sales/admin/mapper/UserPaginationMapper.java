package com.sales.admin.mapper;


import com.sales.entities.UserPagination;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserPaginationMapper {
    UserPagination toDto(UserPagination userPagination);
}
