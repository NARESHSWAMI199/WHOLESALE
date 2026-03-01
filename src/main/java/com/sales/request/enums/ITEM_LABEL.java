package com.sales.request.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ITEM_LABEL {
    OLD("O"),
    NEW("N");
    private final String label;
}
