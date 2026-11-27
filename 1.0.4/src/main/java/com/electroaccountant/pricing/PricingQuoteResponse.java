package com.electroaccountant.pricing;

import java.math.BigDecimal;

public record PricingQuoteResponse(String itemId, String pricingGroupName, BigDecimal mrp, BigDecimal paymentAdjustmentPercent,
        BigDecimal quantityAdjustmentPercent, BigDecimal calculatedRate) { }
