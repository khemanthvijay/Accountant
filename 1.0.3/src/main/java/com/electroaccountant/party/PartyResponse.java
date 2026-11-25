package com.electroaccountant.party;

public record PartyResponse(String id, String name, LedgerType ledgerType, String phone, String email, String address) { }
