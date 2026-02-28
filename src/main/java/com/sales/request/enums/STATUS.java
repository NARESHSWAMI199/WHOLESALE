package com.sales.request.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum STATUS {
    ACTIVE("A"),
    DEACTIVATED("D");
    private final String status;
}
