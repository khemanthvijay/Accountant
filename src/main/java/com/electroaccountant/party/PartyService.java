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

    public PartyResponse create(String type, CreatePartyRequest request) {
        String id = UUID.randomUUID().toString();
        try {
            repository.insert(id, type, request.name().trim(), blankToNull(request.phone()), blankToNull(request.email()), blankToNull(request.address()));
        } catch (org.springframework.dao.DuplicateKeyException exception) {
            throw new ApiException(HttpStatus.CONFLICT, type.toLowerCase() + " already exists: " + request.name().trim());
        }
        return new PartyResponse(id, request.name().trim(), blankToNull(request.phone()), blankToNull(request.email()), blankToNull(request.address()));
    }

    public List<PartyResponse> list(String type) { return repository.findActiveByType(type).stream().map(this::response).toList(); }
    public PartyResponse get(String id, String type) { return response(required(id, type)); }

    public PartyResponse update(String id, String type, UpdatePartyRequest request) {
        required(id, type);
        try {
            repository.update(id, type, request.name().trim(), blankToNull(request.phone()), blankToNull(request.email()), blankToNull(request.address()));
        } catch (org.springframework.dao.DuplicateKeyException exception) {
            throw new ApiException(HttpStatus.CONFLICT, type.toLowerCase() + " already exists: " + request.name().trim());
        }
        return get(id, type);
    }
    public PartyRepository.PartyRow required(String id, String type) {
        return repository.findActive(id, type).orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Active " + type.toLowerCase() + " not found"));
    }
    private PartyResponse response(PartyRepository.PartyRow row) { return new PartyResponse(row.id(), row.name(), row.phone(), row.email(), row.address()); }
    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
