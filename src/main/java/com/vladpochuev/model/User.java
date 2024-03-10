package com.vladpochuev.model;

import lombok.Data;

import java.util.UUID;

@Data
public class User {
    private UUID id;
    private String username;
    private String password;
}
