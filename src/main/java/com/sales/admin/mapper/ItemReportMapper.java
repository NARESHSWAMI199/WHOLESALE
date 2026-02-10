package com.sales.admin.mapper;


import com.sales.admin.dto.ItemReportDto;
import com.sales.entities.ItemReport;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ItemReportMapper {
    ItemReportDto toDto(ItemReport itemReport);
}
