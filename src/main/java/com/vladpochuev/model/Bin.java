package com.vladpochuev.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Bin {
    private String id;
    private String title;
    private String text;
}
