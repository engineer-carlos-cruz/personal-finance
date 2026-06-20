package com.ccruz.personal_finance.expense.web.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseUpsertRequest(
    @NotNull Long expenseCategoryId,

    @NotNull Long accountId,

    @NotNull
    @Digits(integer = 17, fraction = 2)
    BigDecimal amount,

    @NotNull LocalDate date,

    @Size(max = 255)
    String description
) {
}
