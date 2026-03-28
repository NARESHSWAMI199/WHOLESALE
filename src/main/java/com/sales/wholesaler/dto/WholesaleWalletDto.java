package com.sales.wholesaler.dto;

import lombok.Builder;

@Builder
public record WholesaleWalletDto(
    int id,
    Integer userId,
    Float amount
) {
}
