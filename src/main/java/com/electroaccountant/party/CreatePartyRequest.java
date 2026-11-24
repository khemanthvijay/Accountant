package com.electroaccountant.party;

import jakarta.validation.constraints.NotBlank;

public record CreatePartyRequest(
        @NotBlank String name,
        String phone,
        String email,
        String address) { }
