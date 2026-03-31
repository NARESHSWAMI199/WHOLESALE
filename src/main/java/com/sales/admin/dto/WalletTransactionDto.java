package com.sales.admin.dto;


import lombok.Builder;


@Builder
public record WalletTransactionDto(
        String slug,
        Integer userId,
        Float amount,
        Long createdAt,
        String transactionType,
        String status
) {
}
