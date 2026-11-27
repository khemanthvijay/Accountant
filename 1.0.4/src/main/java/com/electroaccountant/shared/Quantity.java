package com.electroaccountant.shared;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Quantity {
    public static final long SCALE = 1_000L;
    private Quantity() { }

    public static long toScaled(BigDecimal value) {
        if (value == null || value.signum() < 0) {
            throw new ApiException(org.springframework.http.HttpStatus.BAD_REQUEST, "Quantity cannot be negative");
        }
        try {
            return value.movePointRight(3).setScale(0, RoundingMode.UNNECESSARY).longValueExact();
        } catch (ArithmeticException exception) {
            throw new ApiException(org.springframework.http.HttpStatus.BAD_REQUEST, "Quantity supports at most three decimal places");
        }
    }

    public static long toPositiveScaled(BigDecimal value) {
        long scaled = toScaled(value);
        if (scaled == 0) {
            throw new ApiException(org.springframework.http.HttpStatus.BAD_REQUEST, "Quantity must be greater than zero");
        }
        return scaled;
    }

    public static BigDecimal fromScaled(long value) {
        return BigDecimal.valueOf(value, 3).stripTrailingZeros();
    }
}
