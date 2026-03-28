package com.sales.request.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ITEM_STOCK {
    INSTOCK("Y"),
    OUT_OF_STOCK("N");
    private final String stock;
}
