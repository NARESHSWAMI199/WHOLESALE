package com.sales.wholesaler.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record WholesaleAddressDto(
        String slug,
        String street,
        String zipCode,
        CityDto city,
        StateDto state,
        Float latitude,
        Float altitude
) {
}
