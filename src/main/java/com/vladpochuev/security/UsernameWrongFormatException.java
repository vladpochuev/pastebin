package com.vladpochuev.security;

import org.springframework.security.core.AuthenticationException;

public class UsernameWrongFormatException extends AuthenticationException {
    public UsernameWrongFormatException(String msg) {
        super(msg);
    }
}
