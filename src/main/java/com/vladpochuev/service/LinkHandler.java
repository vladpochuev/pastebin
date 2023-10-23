package com.vladpochuev.service;

import com.vladpochuev.dao.BinDAO;


public class LinkHandler extends Thread {
    private final String binId;
    private final String amountOfTime;
    private final BinDAO binDAO;

    public LinkHandler(String binId, String amountOfTime, BinDAO binDAO) {
        this.binId = binId;
        this.binDAO = binDAO;
        this.amountOfTime = amountOfTime;
    }

    @Override
    public void run() {
        try {
            Long amountOfTime = getAmountOfTime(this.amountOfTime);
            if(amountOfTime == null) return;
            System.out.println("bin " + binId + " was added to queue for " + amountOfTime + " ms");
            Thread.sleep(amountOfTime);
            binDAO.delete(binId);
            System.out.println("bin " + binId + " was removed");
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
        SIX_MONTH(ONE_MONTH.time * 6);

        private final Long time;
        AmountOfTime(Long time) {
            this.time = time;
        }
    }
}
