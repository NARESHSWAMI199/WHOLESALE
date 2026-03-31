package com.sales.admin.mapper;


import com.sales.admin.dto.FuturePlanDto;
import com.sales.entities.WholesalerFuturePlan;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FuturePlanMapper {
    FuturePlanDto toDto(WholesalerFuturePlan wholesalerFuturePlan);
}
