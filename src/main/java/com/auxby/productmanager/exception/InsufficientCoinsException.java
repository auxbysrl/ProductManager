package com.auxby.productmanager.exception;

public class InsufficientCoinsException extends RuntimeException {

    public InsufficientCoinsException(String action) {
        super(String.format("Fail to '%s'. Not enough coins for this action.", action));
    }
}
