package com.sales.wholesaler.mapper;


import com.sales.entities.ServicePlan;
import com.sales.wholesaler.dto.WholesaleServicePlanDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WholesaleServicePlanMapper {
    WholesaleServicePlanDto toDto(ServicePlan servicePlan);
}
