package com.vladpochuev.security;

import org.springframework.security.core.AuthenticationException;

public class PasswordWrongFormatException extends AuthenticationException {
    public PasswordWrongFormatException(String msg) {
        super(msg);
    }
}
