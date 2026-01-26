package com.sales.wholesaler.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record WholesaleAddressDto(
        int id,
        String slug,
        String street,
        String zipCode,
        Integer city,
        Integer state,
        Float latitude,
        Float altitude
) {
}
