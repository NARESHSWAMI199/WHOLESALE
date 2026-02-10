package com.sales.admin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AddressDto(
        String slug,
        String street,
        String zipCode,
        CityDto city,
        StateDto state,
        Float latitude,
        Float altitude
) {
}
