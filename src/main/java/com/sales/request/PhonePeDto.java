package com.sales.request;

import lombok.*;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PhonePeDto {
    Long amount;
    String merchantTransactionId;
    String xVerify;
    String encodedResponse;
    Integer userId;
}
