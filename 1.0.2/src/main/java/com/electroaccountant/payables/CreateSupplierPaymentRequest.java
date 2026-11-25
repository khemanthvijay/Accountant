package com.electroaccountant.payables;

import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.electroaccountant.receivables.ReceiptMethod;

public record CreateSupplierPaymentRequest(@NotBlank String supplierId, @NotNull LocalDate paymentDate,
        @NotNull @DecimalMin("0.01") BigDecimal amount, @NotNull ReceiptMethod method,
        String referenceNumber, String notes) { }
