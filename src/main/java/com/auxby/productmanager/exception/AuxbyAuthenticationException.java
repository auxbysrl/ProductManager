package com.auxby.productmanager.exception;

import org.springframework.security.core.AuthenticationException;

public class AuxbyAuthenticationException extends AuthenticationException {
    public AuxbyAuthenticationException(String msg) {
        super(msg);
    }
}
