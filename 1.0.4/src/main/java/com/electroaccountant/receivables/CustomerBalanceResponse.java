package com.electroaccountant.receivables;

import java.math.BigDecimal;

public record CustomerBalanceResponse(String customerId, String customerName, BigDecimal creditSales, BigDecimal receipts, BigDecimal outstanding) { }
