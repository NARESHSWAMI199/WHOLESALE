package com.sales.exceptions;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserException extends RuntimeException {
    String message;

    public UserException(String message) {
        super(message);
        this.message = message;
    }

}
