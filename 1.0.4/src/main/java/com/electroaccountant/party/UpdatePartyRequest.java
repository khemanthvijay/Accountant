package com.electroaccountant.party;

import jakarta.validation.constraints.NotBlank;

public record UpdatePartyRequest(
        @NotBlank String name,
        LedgerType ledgerType,
        String phone,
        String email,
        String address) { }
