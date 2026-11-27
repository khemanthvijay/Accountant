package com.electroaccountant.pricing;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.electroaccountant.trade.PaymentType;

public record PricingGroupRequest(
        @NotBlank String name,
        @NotNull List<String> itemIds,
        @NotNull Map<PaymentType, BigDecimal> paymentAdjustments,
        @NotNull List<@Valid PricingQuantityRuleRequest> quantityRules) { }
