package com.electroaccountant.trade;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EntryLineRequest(
        @NotBlank String itemId,
        @NotNull BigDecimal quantity,
        BigDecimal unitAmount) { }
