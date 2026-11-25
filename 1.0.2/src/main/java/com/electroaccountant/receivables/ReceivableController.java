package com.electroaccountant.receivables;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ReceivableController {
    private final ReceivableService service;
    public ReceivableController(ReceivableService service) { this.service = service; }
    @GetMapping("/receipts") List<CustomerReceiptResponse> receipts() { return service.receipts(); }
    @GetMapping("/customers/{customerId}/balance") CustomerBalanceResponse balance(@PathVariable String customerId) { return service.balance(customerId); }
    @GetMapping("/customers/{customerId}/receipts") List<CustomerReceiptResponse> customerReceipts(@PathVariable String customerId) { return service.receipts(customerId); }
    @PostMapping("/receipts") @ResponseStatus(HttpStatus.CREATED) CustomerReceiptResponse create(@Valid @RequestBody CreateCustomerReceiptRequest request) { return service.create(request); }
}
