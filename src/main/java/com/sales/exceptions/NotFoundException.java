package com.sales.exceptions;

public class NotFoundException extends RuntimeException {

    String message;

    public NotFoundException(String message) {
        super(message);
        this.message = message;
    }

}
