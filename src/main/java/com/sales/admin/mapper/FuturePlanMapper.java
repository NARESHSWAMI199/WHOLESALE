package com.sales.admin.mapper;


import com.sales.admin.dto.FuturePlanDto;
import com.sales.entities.WholesalerFuturePlan;
import com.sales.wholesaler.dto.WholesalerFuturePlanDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FuturePlanMapper {
    FuturePlanDto toDto(WholesalerFuturePlan wholesalerFuturePlan);
}
