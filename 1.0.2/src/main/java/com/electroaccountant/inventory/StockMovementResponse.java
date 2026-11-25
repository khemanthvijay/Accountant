package com.electroaccountant.inventory;

import java.math.BigDecimal;
import java.time.LocalDate;

public record StockMovementResponse(
        String id,
        LocalDate movementDate,
        String movementType,
        BigDecimal quantityDelta,
        String sourceType,
        String sourceId) { }
