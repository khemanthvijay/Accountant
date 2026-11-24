package com.electroaccountant.party;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PartyController {
    private final PartyService service;
    public PartyController(PartyService service) { this.service = service; }

    @GetMapping("/suppliers") List<PartyResponse> suppliers() { return service.list("SUPPLIER"); }
    @PostMapping("/suppliers") @ResponseStatus(HttpStatus.CREATED)
    PartyResponse createSupplier(@Valid @RequestBody CreatePartyRequest request) { return service.create("SUPPLIER", request); }
    @GetMapping("/suppliers/{id}") PartyResponse supplier(@org.springframework.web.bind.annotation.PathVariable String id) { return service.get(id, "SUPPLIER"); }
    @PutMapping("/suppliers/{id}") PartyResponse updateSupplier(@org.springframework.web.bind.annotation.PathVariable String id, @Valid @RequestBody UpdatePartyRequest request) { return service.update(id, "SUPPLIER", request); }

    @GetMapping("/customers") List<PartyResponse> customers() { return service.list("CUSTOMER"); }
    @PostMapping("/customers") @ResponseStatus(HttpStatus.CREATED)
    PartyResponse createCustomer(@Valid @RequestBody CreatePartyRequest request) { return service.create("CUSTOMER", request); }
    @GetMapping("/customers/{id}") PartyResponse customer(@org.springframework.web.bind.annotation.PathVariable String id) { return service.get(id, "CUSTOMER"); }
    @PutMapping("/customers/{id}") PartyResponse updateCustomer(@org.springframework.web.bind.annotation.PathVariable String id, @Valid @RequestBody UpdatePartyRequest request) { return service.update(id, "CUSTOMER", request); }
}
