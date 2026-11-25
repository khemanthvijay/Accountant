package com.electroaccountant.reports;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
public record ProfitLossResponse(LocalDate fromDate,LocalDate toDate,BigDecimal sales,BigDecimal costOfGoodsSold,BigDecimal grossProfit,Map<String,BigDecimal> expenses,BigDecimal totalExpenses,BigDecimal netProfit) { }
