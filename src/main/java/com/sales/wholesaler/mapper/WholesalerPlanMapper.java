package com.sales.wholesaler.mapper;

import com.sales.entities.WholesalerPlans;
import com.sales.wholesaler.dto.WholesalerPlanDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WholesalerPlanMapper {
    WholesalerPlanDto toDto(WholesalerPlans wholesalerPlans);
}
