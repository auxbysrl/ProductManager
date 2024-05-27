package com.auxby.productmanager.exception;

public class PhotoUploadException extends RuntimeException {
    public PhotoUploadException() {
        super("Fail to upload images for offer.");
    }

    public PhotoUploadException(String message) {
        super("Fail to upload images for offer. " + message);
    }
}
