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
}
