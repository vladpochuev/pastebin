package com.vladpochuev.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
