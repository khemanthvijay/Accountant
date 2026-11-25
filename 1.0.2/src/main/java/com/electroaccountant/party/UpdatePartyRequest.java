package com.electroaccountant.party;

import jakarta.validation.constraints.NotBlank;

public record UpdatePartyRequest(
        @NotBlank String name,
        String phone,
        String email,
        String address) { }
