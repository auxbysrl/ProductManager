package com.auxby.productmanager.exception;

public class DeepLinkGenerationException extends RuntimeException {

    public DeepLinkGenerationException() {
        super("Fail to generate deep link for offer.");
    }
}
