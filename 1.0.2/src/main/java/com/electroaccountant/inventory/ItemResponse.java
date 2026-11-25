package com.electroaccountant.inventory;

import java.math.BigDecimal;

public record ItemResponse(
        String id,
        String sku,
        String name,
        String description,
        String alias,
        String category,
        String unit,
        BigDecimal availableQuantity,
        BigDecimal reorderLevel,
        BigDecimal openingRate,
        BigDecimal defaultPurchaseRate,
        BigDecimal defaultSalesRate,
        boolean lowStock) { }
