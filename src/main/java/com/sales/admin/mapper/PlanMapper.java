package com.sales.admin.mapper;

import com.sales.admin.dto.PlanDto;
import com.sales.entities.WholesalerPlans;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PlanMapper {
    PlanDto toDto(WholesalerPlans Plans);
}
