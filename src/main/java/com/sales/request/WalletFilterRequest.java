package com.sales.request;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class WalletFilterRequest extends SearchFilters {
    private String slug;
}
