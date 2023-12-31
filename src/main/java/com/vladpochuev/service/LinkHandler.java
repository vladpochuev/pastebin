package com.vladpochuev.service;

import com.vladpochuev.dao.BinDAO;
import com.vladpochuev.model.BinEntity;
import com.vladpochuev.model.BinNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class LinkHandler {
    private final BinDAO binDAO;
    private final SimpMessagingTemplate messagingTemplate;
    private final FirestoreMessageService messageService;
    private final static String pattern = "yyyy-MM-dd HH:mm:ss";

    @Autowired
    public LinkHandler(BinDAO binDAO, SimpMessagingTemplate messagingTemplate, FirestoreMessageService messageService) {
        this.binDAO = binDAO;
        this.messagingTemplate = messagingTemplate;
        this.messageService = messageService;
    }

    public String getExpirationTime(String amountOfTime) {
        Long time = AmountOfTime.valueOf(amountOfTime).time;
        if (time == null) return null;
        ZonedDateTime expirationTime = ZonedDateTime.now().plus(time, ChronoUnit.MILLIS);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern);
        return expirationTime.format(dtf);
    }

    @Scheduled(fixedDelay = 5000)
    public void deleteExpiredBins() {
        List<BinEntity> binEntities = binDAO.readExpired();
        for (BinEntity binEntity : binEntities) {
            binDAO.delete(binEntity.getId());
            messageService.sendDelete(binEntity.getMessageUUID());
            messagingTemplate.convertAndSend("/topic/deletedBinNotifications",
                    new ResponseEntity<>(BinNotification.getFromBinEntity(binEntity), HttpStatus.OK));
        }
    }

    enum AmountOfTime {
        INFINITE(null),
        ONE_MINUTE(1000 * 60L),
        TEN_MINUTES(ONE_MINUTE.time * 10),
        ONE_HOUR(ONE_MINUTE.time * 60),
        ONE_DAY(ONE_HOUR.time * 24),
        ONE_WEEK(ONE_DAY.time * 7),
        ONE_MONTH(ONE_DAY.time * 30),
        SIX_MONTHS(ONE_MONTH.time * 6);

        private final Long time;

        AmountOfTime(Long time) {
            this.time = time;
        }
    }
}
