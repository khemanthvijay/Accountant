package com.electroaccountant.expenses;
import jakarta.validation.constraints.NotBlank;
public record CreateExpenseTypeRequest(@NotBlank String name) { }
