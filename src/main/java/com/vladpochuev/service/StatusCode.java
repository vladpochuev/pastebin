package com.vladpochuev.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Getter
public enum StatusCode {
    OK(0),
    ERROR(1);

    private int code;

    StatusCode(int code) {
        this.code = code;
    }
}
