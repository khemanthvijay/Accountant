package com.electroaccountant.reports;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TradingReportResponse(
        LocalDate fromDate,
        LocalDate toDate,
        BigDecimal openingStockValue,
        BigDecimal purchases,
        BigDecimal sales,
        BigDecimal closingStockValue,
        BigDecimal costOfGoodsSold,
        BigDecimal grossProfit) { }
