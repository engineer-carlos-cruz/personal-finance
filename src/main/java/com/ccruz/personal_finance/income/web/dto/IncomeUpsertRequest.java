package com.ccruz.personal_finance.income.web.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IncomeUpsertRequest(
    @NotNull Long incomeCategoryId,

    @NotNull Long accountId,

    @NotNull
    @Digits(integer = 17, fraction = 2)
    BigDecimal amount,

    @NotNull LocalDate date,

    @Size(max = 255)
    String description
) {
}
