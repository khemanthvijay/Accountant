package com.electroaccountant.journal;
import java.time.LocalDate;
import java.util.List;
public record JournalEntryResponse(String id, LocalDate entryDate, String sourceType, String narration, List<JournalLineResponse> lines) { }
