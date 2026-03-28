package com.sales.request;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CashfreeFilters  extends SearchFilters{
    String transactionId;
    String paymentStatus;
}
