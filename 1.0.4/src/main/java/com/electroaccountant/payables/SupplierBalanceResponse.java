package com.electroaccountant.payables;

import java.math.BigDecimal;
public record SupplierBalanceResponse(String supplierId, String supplierName, BigDecimal creditPurchases,
        BigDecimal creditExpenses, BigDecimal payments, BigDecimal outstanding) { }
