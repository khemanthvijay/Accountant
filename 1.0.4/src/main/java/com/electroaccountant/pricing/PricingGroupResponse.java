package com.electroaccountant.pricing;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import com.electroaccountant.trade.PaymentType;

public record PricingGroupResponse(String id, String name, List<String> itemIds,
        Map<PaymentType, BigDecimal> paymentAdjustments, List<PricingQuantityRuleRequest> quantityRules) { }
