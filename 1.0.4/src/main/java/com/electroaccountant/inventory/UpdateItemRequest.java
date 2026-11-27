package com.electroaccountant.inventory;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;

public record UpdateItemRequest(
        @NotBlank String sku,
        @NotBlank String name,
        String description,
        String alias,
        String category,
        @NotBlank String unit,
        BigDecimal defaultPurchaseRate,
        BigDecimal defaultSalesRate,
        BigDecimal reorderLevel) { }
