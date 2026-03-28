package com.sales.admin.mapper;


import com.sales.admin.dto.ServicePlanDto;
import com.sales.entities.ServicePlan;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ServicePlanMapper {
    ServicePlanDto toDto(ServicePlan servicePlan);
}
