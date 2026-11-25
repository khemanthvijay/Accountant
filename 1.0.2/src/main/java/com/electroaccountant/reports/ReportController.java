package com.electroaccountant.reports;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final InventoryValuationService valuationService;
    private final ProfitLossService profitLossService;
    public ReportController(InventoryValuationService valuationService, ProfitLossService profitLossService) { this.valuationService = valuationService; this.profitLossService = profitLossService; }
    @GetMapping("/trading")
    TradingReportResponse trading(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return valuationService.trading(from, to);
    }
    @GetMapping("/profit-loss")
    ProfitLossResponse profitLoss(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return profitLossService.report(from, to);
    }
}
