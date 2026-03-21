package com.sales.admin.mapper;


import com.sales.admin.dto.PermissionDto;
import com.sales.entities.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    PermissionDto toDto(Permission permission);
}
