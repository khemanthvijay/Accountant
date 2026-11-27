package com.electroaccountant.transactions;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionResponse(
        String id,
        String type,
        LocalDate date,
        String partyId,
        String partyName,
        String reference,
        String notes,
        BigDecimal amount) { }
