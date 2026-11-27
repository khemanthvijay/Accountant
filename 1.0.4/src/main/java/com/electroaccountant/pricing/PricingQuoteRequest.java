package com.electroaccountant.pricing;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.electroaccountant.trade.PaymentType;

public record PricingQuoteRequest(@NotBlank String itemId, @NotNull BigDecimal quantity, @NotNull PaymentType paymentType) { }
