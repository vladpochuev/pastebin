package com.vladpochuev.model;

import com.vladpochuev.service.StatusCode;
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
    private String code;

    public static BinNotification getFromBin(Bin bin, StatusCode statusCode) {
        return new BinNotification(bin.getId(), bin.getTitle(), bin.getX(), bin.getY(),
                bin.getColor(), statusCode.name());
    }
}
