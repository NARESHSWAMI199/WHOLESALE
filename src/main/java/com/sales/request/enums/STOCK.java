package com.sales.request.enums;


import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum STOCK {
    INSTOCK("Y"),
    OUT_OF_STOCK("N");
    private final String stock;
}
