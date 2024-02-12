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

import java.time.Duration;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAmount;
import java.util.List;

@Service
public class LinkHandler {
    private final BinDAO binDAO;
    private final SimpMessagingTemplate messagingTemplate;
    private final FirestoreMessageService messageService;
    private String dateTimePattern = "yyyy-MM-dd HH:mm:ss";

    @Autowired
    public LinkHandler(BinDAO binDAO, SimpMessagingTemplate messagingTemplate, FirestoreMessageService messageService) {
        this.binDAO = binDAO;
        this.messagingTemplate = messagingTemplate;
        this.messageService = messageService;
    }

    public String getExpirationTime(String amountOfTime) {
        TemporalAmount time = AmountOfTime.valueOf(amountOfTime).time;
        if (time == null) return null;
        ZonedDateTime expirationTime = ZonedDateTime.now().plus(time);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(dateTimePattern);
        return expirationTime.format(dtf);
    }

    @Scheduled(fixedDelay = 5000)
    public void deleteExpiredBins() {
        List<BinEntity> binEntities = binDAO.readExpired();
        for (BinEntity binEntity : binEntities) {
            binDAO.delete(binEntity.getId());
            this.messageService.sendDelete(binEntity.getMessageUUID());
            this.messagingTemplate.convertAndSend("/topic/deletedBinNotifications",
                    new ResponseEntity<>(BinNotification.getFromBinEntity(binEntity), HttpStatus.OK));
        }
    }

    public String getDateTimePattern() {
        return dateTimePattern;
    }

    public void setDateTimePattern(String dateTimePattern) {
        this.dateTimePattern = dateTimePattern;
    }

    enum AmountOfTime {
        INFINITE(null),
        ONE_MINUTE(Duration.ofMinutes(1)),
        TEN_MINUTES(Duration.ofMinutes(10)),
        ONE_HOUR(Duration.ofHours(1)),
        ONE_DAY(Period.ofDays(1)),
        ONE_WEEK(Period.ofWeeks(1)),
        ONE_MONTH(Period.ofMonths(1)),
        SIX_MONTHS(Period.ofMonths(6));

        private final TemporalAmount time;

        AmountOfTime(TemporalAmount time) {
            this.time = time;
        }
    }
}
