package com.vladpochuev.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Bin implements Placeable {
    private String id;
    private String title;
    private String message;
    private String amountOfTime;
    private Integer x;
    private Integer y;
}

