package com.electroaccountant.party;

public enum LedgerType {
    CUSTOMER, SUPPLIER, CUSTOMER_AND_SUPPLIER, CAPITAL, CASH, BANK, EXPENSE, INCOME, ASSET, LIABILITY, OTHER;

    public boolean canActAsCustomer() {
        return this == CUSTOMER || this == CUSTOMER_AND_SUPPLIER;
    }

    public boolean canActAsSupplier() {
        return this == SUPPLIER || this == CUSTOMER_AND_SUPPLIER;
    }

    // Kept only for compatibility with the 1.0.2 SQLite CHECK constraint.
    public String legacyPartyType() {
        return this == SUPPLIER ? "SUPPLIER" : "CUSTOMER";
    }
}
