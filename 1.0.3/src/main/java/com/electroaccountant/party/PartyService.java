package com.electroaccountant.party;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.electroaccountant.shared.ApiException;

@Service
public class PartyService {
    private final PartyRepository repository;
    public PartyService(PartyRepository repository) { this.repository = repository; }

    public PartyResponse create(LedgerType ledgerType, CreatePartyRequest request) {
        String id = UUID.randomUUID().toString();
        try {
            repository.insert(id, ledgerType, request.name().trim(), blankToNull(request.phone()), blankToNull(request.email()), blankToNull(request.address()));
        } catch (org.springframework.dao.DuplicateKeyException exception) {
            throw new ApiException(HttpStatus.CONFLICT, "Ledger already exists: " + request.name().trim());
        }
        return new PartyResponse(id, request.name().trim(), ledgerType, blankToNull(request.phone()), blankToNull(request.email()), blankToNull(request.address()));
    }

    public PartyResponse create(CreatePartyRequest request) {
        if (request.ledgerType() == null) throw new ApiException(HttpStatus.BAD_REQUEST, "Ledger type is required");
        return create(request.ledgerType(), request);
    }
    public List<PartyResponse> ledgers() { return repository.findAllActive().stream().map(this::response).toList(); }
    public List<PartyResponse> customers() { return repository.findAllActive().stream().filter(row -> row.ledgerType().canActAsCustomer()).map(this::response).toList(); }
    public List<PartyResponse> suppliers() { return repository.findAllActive().stream().filter(row -> row.ledgerType().canActAsSupplier()).map(this::response).toList(); }
    public PartyResponse get(String id) { return response(required(id)); }

    public PartyResponse update(String id, UpdatePartyRequest request) {
        PartyRepository.PartyRow existing = required(id);
        LedgerType ledgerType = request.ledgerType() == null ? existing.ledgerType() : request.ledgerType();
        try {
            repository.update(id, ledgerType, request.name().trim(), blankToNull(request.phone()), blankToNull(request.email()), blankToNull(request.address()));
        } catch (org.springframework.dao.DuplicateKeyException exception) {
            throw new ApiException(HttpStatus.CONFLICT, "Ledger already exists: " + request.name().trim());
        }
        return get(id);
    }
    public PartyRepository.PartyRow required(String id) {
        return repository.findActive(id).orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Active ledger not found"));
    }
    public PartyRepository.PartyRow requiredCustomer(String id) {
        PartyRepository.PartyRow row = required(id);
        if (!row.ledgerType().canActAsCustomer()) throw new ApiException(HttpStatus.BAD_REQUEST, "Ledger is not a customer/consumer");
        return row;
    }
    public PartyRepository.PartyRow requiredSupplier(String id) {
        PartyRepository.PartyRow row = required(id);
        if (!row.ledgerType().canActAsSupplier()) throw new ApiException(HttpStatus.BAD_REQUEST, "Ledger is not a supplier");
        return row;
    }
    private PartyResponse response(PartyRepository.PartyRow row) { return new PartyResponse(row.id(), row.name(), row.ledgerType(), row.phone(), row.email(), row.address()); }
    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
