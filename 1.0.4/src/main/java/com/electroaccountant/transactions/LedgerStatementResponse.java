package com.electroaccountant.transactions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record LedgerStatementResponse(String partyId, String partyName, LocalDate fromDate, LocalDate toDate,
        BigDecimal openingBalance, BigDecimal sales, BigDecimal purchases, BigDecimal received, BigDecimal paid,
        BigDecimal closingBalance, List<LedgerStatementLineResponse> lines) { }
