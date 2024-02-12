package com.vladpochuev.service;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;

import java.time.Duration;
import java.time.Period;

public class TemporalAmountConverter implements ArgumentConverter {
    @Override
    public Object convert(Object o, ParameterContext parameterContext) throws ArgumentConversionException {
        try {
            String temporalAmount = (String) o;
            if (temporalAmount.startsWith("PT")) {
                return Duration.parse(temporalAmount);
            } else if (temporalAmount.startsWith("P")) {
                return Period.parse(temporalAmount);
            } else {
                throw new IllegalArgumentException();
            }
        } catch (ClassCastException e) {
            throw new IllegalArgumentException();
        }
    }
}
