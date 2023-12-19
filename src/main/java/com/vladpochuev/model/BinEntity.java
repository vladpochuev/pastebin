package com.vladpochuev.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BinEntity implements Placeable {
    private String id;
    private String title;
    private String messageUUID;
    private Integer x;
    private Integer y;
    private String color;
    private String amountOfTime;
    private String expirationTime;
    private String username;
}

