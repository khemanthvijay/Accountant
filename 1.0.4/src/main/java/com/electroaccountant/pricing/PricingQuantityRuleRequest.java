package com.electroaccountant.pricing;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;

public record PricingQuantityRuleRequest(
        @NotNull BigDecimal minimumQuantity,
        BigDecimal maximumQuantity,
        @NotNull BigDecimal adjustmentPercent) { }
