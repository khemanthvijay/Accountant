package com.electroaccountant.trade;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TradeEntryController {
    private final TradeEntryService tradeEntryService;
    public TradeEntryController(TradeEntryService tradeEntryService) { this.tradeEntryService = tradeEntryService; }

    @PostMapping("/purchases")
    @ResponseStatus(HttpStatus.CREATED)
    TradeEntryResponse createPurchase(@Valid @RequestBody CreatePurchaseRequest request) {
        return tradeEntryService.createPurchase(request);
    }
    @PutMapping("/purchases/{id}") TradeEntryResponse updatePurchase(@PathVariable String id, @Valid @RequestBody CreatePurchaseRequest request) { return tradeEntryService.updatePurchase(id, request); }
    @GetMapping("/purchases/{id}") TradeEntryResponse purchase(@PathVariable String id) { return tradeEntryService.purchase(id); }

    @PostMapping("/sales")
    @ResponseStatus(HttpStatus.CREATED)
    TradeEntryResponse createSale(@Valid @RequestBody CreateSaleRequest request) {
        return tradeEntryService.createSale(request);
    }
    @PutMapping("/sales/{id}") TradeEntryResponse updateSale(@PathVariable String id, @Valid @RequestBody CreateSaleRequest request) { return tradeEntryService.updateSale(id, request); }
    @GetMapping("/sales/{id}") TradeEntryResponse sale(@PathVariable String id) { return tradeEntryService.sale(id); }

    @GetMapping("/purchases")
    List<TradeEntryResponse> purchases(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return tradeEntryService.dailyPurchases(date);
    }

    @GetMapping("/sales")
    List<TradeEntryResponse> sales(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return tradeEntryService.dailySales(date);
    }
}
