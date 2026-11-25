package com.electroaccountant.expenses;
import java.math.BigDecimal;
import java.time.LocalDate;
public record ExpenseResponse(String id, LocalDate expenseDate, String expenseTypeId, String expenseTypeName, String description,
        String notes, BigDecimal amount) { }
