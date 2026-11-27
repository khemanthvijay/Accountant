package com.electroaccountant.transactions;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LedgerStatementLineResponse(LocalDate date, String type, String reference, String notes,
        BigDecimal debit, BigDecimal credit, BigDecimal balance) { }
