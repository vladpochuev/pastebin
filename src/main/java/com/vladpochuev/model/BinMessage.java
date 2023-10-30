package com.vladpochuev.model;

import com.vladpochuev.service.StatusCode;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BinMessage {
    private String id;
    private String title;
    private String message;
    private Integer x;
    private Integer y;
    private int code;

    public static BinMessage getFromBin(Bin bin, StatusCode statusCode) {
        if(bin == null) return null;
        return new BinMessage(bin.getId(), bin.getTitle(), bin.getMessage(), bin.getX(), bin.getY(), statusCode.getCode());
    }
}
