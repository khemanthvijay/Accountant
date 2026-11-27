package com.electroaccountant.shared;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Money {
    private Money() { }

    public static long toMinor(BigDecimal amount) {
        if (amount == null || amount.signum() < 0) {
            throw new ApiException(org.springframework.http.HttpStatus.BAD_REQUEST, "Amount cannot be negative");
        }
        try {
            return amount.movePointRight(2).setScale(0, RoundingMode.UNNECESSARY).longValueExact();
        } catch (ArithmeticException exception) {
            throw new ApiException(org.springframework.http.HttpStatus.BAD_REQUEST, "Amount supports at most two decimal places");
        }
    }

    public static BigDecimal fromMinor(long amount) {
        return BigDecimal.valueOf(amount, 2);
    }

    public static long lineTotal(long quantityScaled, long unitPriceMinor) {
        return BigDecimal.valueOf(quantityScaled)
                .multiply(BigDecimal.valueOf(unitPriceMinor))
                .divide(BigDecimal.valueOf(Quantity.SCALE), 0, RoundingMode.HALF_UP)
                .longValueExact();
    }
}
