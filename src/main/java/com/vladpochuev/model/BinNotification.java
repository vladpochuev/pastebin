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
    private Integer x;
    private Integer y;
    private String color;

    public static BinNotification getFromBin(Bin bin) {
        return new BinNotification(bin.getId(), bin.getTitle(), bin.getX(), bin.getY(),
                bin.getColor());
    }
}
