package com.sales.exceptions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MyException extends RuntimeException {
    String message;

    public MyException(String message) {
        super(message);
        this.message = message;
    }

}
