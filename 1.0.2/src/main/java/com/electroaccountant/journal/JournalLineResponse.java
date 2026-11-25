package com.electroaccountant.journal;
import java.math.BigDecimal;
public record JournalLineResponse(String accountCode, String accountName, BigDecimal debit, BigDecimal credit) { }
