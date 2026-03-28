package com.sales.wholesaler.mapper;


import com.sales.entities.WholesalerFuturePlan;
import com.sales.wholesaler.dto.WholesalerFuturePlanDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WholesalerFuturePlanMapper {
    WholesalerFuturePlanDto toDto(WholesalerFuturePlan wholesalerFuturePlan);
}
