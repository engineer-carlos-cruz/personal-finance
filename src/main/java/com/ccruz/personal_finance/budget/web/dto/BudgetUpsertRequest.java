package com.ccruz.personal_finance.budget.web.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record BudgetUpsertRequest(
    @NotNull Long expenseCategoryId,

    @NotNull @Digits(integer = 19, fraction = 2) BigDecimal amount,

    @NotNull LocalDate initialDate,

    @NotNull LocalDate finalDate
) {
}
