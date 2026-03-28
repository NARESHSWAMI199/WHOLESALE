package com.sales.wholesaler.dto;


import lombok.Builder;

@Builder
public record CityDto(
    int id,
    String cityName,
    Integer stateId
) {
}
