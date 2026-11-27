package com.electroaccountant.receivables;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CustomerReceiptResponse(String id, String customerId, String customerName, LocalDate receiptDate,
                                      BigDecimal amount, ReceiptMethod method, String referenceNumber, String notes) { }
