package com.electroaccountant.receivables;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCustomerReceiptRequest(
        @NotBlank String customerId,
        @NotNull LocalDate receiptDate,
        @NotNull BigDecimal amount,
        @NotNull ReceiptMethod method,
        String referenceNumber,
        String notes) { }
