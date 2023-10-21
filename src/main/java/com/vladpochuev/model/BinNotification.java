package com.vladpochuev.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BinNotification {
    private String id;
    private String title;
    private int x;
    private int y;
}
