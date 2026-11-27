package com.electroaccountant.pricing;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pricing")
public class PricingController {
    private final PricingService service;
    public PricingController(PricingService service) { this.service = service; }
    @GetMapping("/groups") List<PricingGroupResponse> groups() { return service.groups(); }
    @GetMapping("/groups/{id}") PricingGroupResponse group(@PathVariable String id) { return service.group(id); }
    @PostMapping("/groups") @ResponseStatus(HttpStatus.CREATED) PricingGroupResponse create(@Valid @RequestBody PricingGroupRequest request) { return service.create(request); }
    @PutMapping("/groups/{id}") PricingGroupResponse update(@PathVariable String id, @Valid @RequestBody PricingGroupRequest request) { return service.update(id, request); }
    @PostMapping("/quote") PricingQuoteResponse quote(@Valid @RequestBody PricingQuoteRequest request) { return service.quote(request); }
}
