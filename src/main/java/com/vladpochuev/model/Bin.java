package com.vladpochuev.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Bin {
    private String title;
    private String message;
    private Integer x;
    private Integer y;
    private String color;
    private String amountOfTime;
}
