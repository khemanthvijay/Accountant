package com.electroaccountant.payables;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api")
public class PayableController {
    private final PayableService service; public PayableController(PayableService service) { this.service=service; }
    @GetMapping("/supplier-payments") List<SupplierPaymentResponse> payments() { return service.payments(); }
    @GetMapping("/suppliers/{supplierId}/balance") SupplierBalanceResponse balance(@PathVariable String supplierId) { return service.balance(supplierId); }
    @PostMapping("/supplier-payments") @ResponseStatus(HttpStatus.CREATED) SupplierPaymentResponse create(@Valid @RequestBody CreateSupplierPaymentRequest request) { return service.create(request); }
}
