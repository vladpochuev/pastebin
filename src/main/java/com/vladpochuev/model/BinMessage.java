package com.vladpochuev.model;

import com.vladpochuev.service.FirestoreMessageService;
import lombok.*;

import java.util.concurrent.ExecutionException;

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

    public static BinMessage getFromBinEntity(BinEntity bin, FirestoreMessageService messageService) {
        if(bin == null) return null;
        try {
            FirestoreMessageEntity entity = messageService.read(bin.getMessageUUID());
            return new BinMessage(bin.getId(), bin.getTitle(), entity.getMessage(), bin.getX(), bin.getY(),
                    bin.getExpirationTime(), bin.getUsername());
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
