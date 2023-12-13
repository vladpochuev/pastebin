package com.vladpochuev.model;

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
    private String expirationTime;
    private String username;

    public static BinMessage getFromBin(Bin bin) {
        if(bin == null) return null;
        return new BinMessage(bin.getId(), bin.getTitle(), bin.getMessage(), bin.getX(), bin.getY(),
                bin.getExpirationTime(), bin.getUsername());
    }
}
