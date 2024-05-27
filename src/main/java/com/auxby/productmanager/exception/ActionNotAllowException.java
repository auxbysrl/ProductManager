package com.auxby.productmanager.exception;

public class ActionNotAllowException extends RuntimeException {
    public ActionNotAllowException() {
        super("Edit offer set as auction not allow.");
    }

    public ActionNotAllowException(String message) {
        super(message);
    }
}
