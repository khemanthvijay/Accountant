package com.electroaccountant.trade;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateSaleRequest(
        @NotNull LocalDate entryDate,
        @NotBlank String customerId,
        @NotNull PaymentType paymentType,
        String invoiceNumber,
        String notes,
        @NotEmpty List<@Valid EntryLineRequest> lines) { }
