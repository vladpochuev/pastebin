package com.vladpochuev.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FirestoreMessageEntity {
    private String UUID;
    private String message;
}
