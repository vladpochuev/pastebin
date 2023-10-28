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
    private int x;
    private int y;

    public static BinMessage getFromBin(Bin bin) {
        return new BinMessage(bin.getId(), bin.getTitle(), bin.getMessage(), bin.getX(), bin.getY());
    }
}
