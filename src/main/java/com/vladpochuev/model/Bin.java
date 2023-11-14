package com.vladpochuev.model;

import lombok.*;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Bin implements Placeable {
    private String id;
    private String title;
    private String message;
    private Integer x;
    private Integer y;
    private String color;
    private String amountOfTime;
    private String expirationTime;
}

