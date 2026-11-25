package com.electroaccountant.expenses;
import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.validation.constraints.*;
public record CreateExpenseRequest(@NotNull LocalDate expenseDate, @NotBlank String expenseTypeId,
        String description, @NotNull @DecimalMin("0.01") BigDecimal amount, String notes) { }
