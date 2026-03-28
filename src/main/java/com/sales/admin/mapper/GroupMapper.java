package com.sales.admin.mapper;

import com.sales.admin.dto.GroupDto;
import com.sales.entities.Group;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GroupMapper {
    GroupDto toDto(Group group);

    Group toEntity(GroupDto groupDto);
}
