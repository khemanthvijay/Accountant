package com.electroaccountant.inventory;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateItemRequest(
        @NotBlank String sku,
        @NotBlank String name,
        String description,
        String alias,
        String category,
        @NotBlank String unit,
        @NotNull BigDecimal openingQuantity,
        BigDecimal openingRate,
        BigDecimal defaultPurchaseRate,
        BigDecimal defaultSalesRate,
        BigDecimal reorderLevel) { }
