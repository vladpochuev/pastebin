package com.vladpochuev.service;

import com.vladpochuev.config.PastebinApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAmount;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = PastebinApplication.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@DisplayName("Defining an expiration time using the LinkHandler class")
class LinkHandlerTest {
    LinkHandler linkHandler;

    public LinkHandlerTest(LinkHandler linkHandler) {
        this.linkHandler = linkHandler;
    }

    @Test
    @DisplayName("Should be null if the amount of time is infinite")
    void testInfiniteTime() {
        String amountOfTime = "INFINITE";
        String expTime = linkHandler.getExpirationTime(amountOfTime);
        assertThat(expTime, nullValue());
    }

    @Test
    @DisplayName("Wrong value should throw exception")
    void testWrongValue() {
        String amountOfTime = "TWO_HOURS";
        assertThrows(Exception.class, () -> linkHandler.getExpirationTime(amountOfTime));
    }

    @ParameterizedTest(name = "[{index}] time = {0}")
    @DisplayName("Should return correct date")
    @CsvSource(value = {
            "ONE_MINUTE, PT1M",
            "TEN_MINUTES, PT10M",
            "ONE_HOUR, PT1H",
            "ONE_DAY, P1D",
            "ONE_WEEK, P1W",
            "ONE_MONTH, P1M",
            "SIX_MONTHS, P6M",
    })
    void testValidValues(String amountOfTime, @ConvertWith(TemporalAmountConverter.class) TemporalAmount temporalAmount) {
        String expTimeString = linkHandler.getExpirationTime(amountOfTime);
        DateTimeFormatter pattern = DateTimeFormatter.ofPattern(linkHandler.getDateTimePattern());
        LocalDateTime expTime = LocalDateTime.parse(expTimeString, pattern);
        LocalDateTime expTimeWithoutPeriod = expTime.minus(temporalAmount);

        double expected = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        double actual = expTimeWithoutPeriod.toEpochSecond(ZoneOffset.UTC);
        double error = Duration.ofSeconds(1).toSeconds();
        assertThat(expected, closeTo(actual, error));
    }
}