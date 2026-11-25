package com.electroaccountant.payables;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.electroaccountant.receivables.ReceiptMethod;
public record SupplierPaymentResponse(String id, String supplierId, String supplierName, LocalDate paymentDate,
        BigDecimal amount, ReceiptMethod method, String referenceNumber, String notes) { }
