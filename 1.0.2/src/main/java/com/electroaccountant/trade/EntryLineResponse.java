package com.electroaccountant.trade;

import java.math.BigDecimal;

public record EntryLineResponse(
        String itemId,
        String itemName,
        BigDecimal quantity,
        BigDecimal unitAmount,
        BigDecimal lineTotal) { }
