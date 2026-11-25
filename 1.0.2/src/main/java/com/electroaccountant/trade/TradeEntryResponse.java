package com.electroaccountant.trade;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TradeEntryResponse(
        String id,
        String type,
        LocalDate entryDate,
        String partyId,
        String partyName,
        String paymentType,
        String documentNumber,
        String notes,
        BigDecimal total,
        List<EntryLineResponse> lines) { }
