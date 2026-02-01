package com.sales.wholesaler.dto;


import lombok.*;


@Builder
public record WholesaleWalletTransactionDto(
    String slug,
    Integer userId,
    Float amount,
    Long createdAt,
    String transactionType,
    String status
) {
}
