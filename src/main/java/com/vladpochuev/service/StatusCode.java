package com.vladpochuev.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum StatusCode {
    OK,
    DUPLICATE,
    NO_SUCH_BIN,
    SERVER_ERROR,
    DATA_ERROR
}
