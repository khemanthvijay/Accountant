package com.electroaccountant.trade;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreatePurchaseRequest(
        @NotNull LocalDate entryDate,
        @NotBlank String supplierId,
        @NotNull PaymentType paymentType,
        String supplierInvoiceNumber,
        String notes,
        @NotEmpty List<@Valid EntryLineRequest> lines) { }
