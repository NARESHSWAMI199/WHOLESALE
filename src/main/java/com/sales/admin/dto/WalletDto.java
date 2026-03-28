package com.sales.admin.dto;

import lombok.Builder;

@Builder
public record WalletDto(
    int id,
    Integer userId,
    Float amount
) {
}
