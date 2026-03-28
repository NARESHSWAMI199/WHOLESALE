package com.sales.request;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class WalletTransactionRequest {

    String slug;
    String status;
    Float amount;
    Integer userId;
    String transactionType;
}
