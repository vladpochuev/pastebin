package com.vladpochuev.service;

import com.vladpochuev.dao.BinDAO;
import com.vladpochuev.model.Bin;
import com.vladpochuev.model.BinNotification;
import org.springframework.messaging.simp.SimpMessagingTemplate;

public class LinkHandler extends Thread {
    private final Bin bin;
    private final String amountOfTime;
    private final BinDAO binDAO;
    private final SimpMessagingTemplate template;

    public LinkHandler(Bin bin, String amountOfTime, BinDAO binDAO, SimpMessagingTemplate template) {
        this.bin = bin;
        this.binDAO = binDAO;
        this.amountOfTime = amountOfTime;
        this.template = template;
    }

    @Override
    public void run() {
        try {
            Long amountOfTime = getAmountOfTime(this.amountOfTime);
            if(amountOfTime == null) return;
            System.out.println("bin " + bin.getId() + " was added to queue for " + amountOfTime + " ms");
            Thread.sleep(amountOfTime);
            binDAO.delete(bin.getId());
            template.convertAndSend("/topic/deletedBinNotifications", BinNotification.getFromBin(bin, StatusCode.OK));
            System.out.println("bin " + bin.getId() + " was removed");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private Long getAmountOfTime(String time) {
        AmountOfTime amountOfTime = AmountOfTime.valueOf(time);
        return amountOfTime.time;
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
