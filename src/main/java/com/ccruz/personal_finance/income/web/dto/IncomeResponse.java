package com.ccruz.personal_finance.income.web.dto;

import com.ccruz.personal_finance.account.web.dto.AccountResponse;
import com.ccruz.personal_finance.income_category.web.dto.IncomeCategoryResponse;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IncomeResponse(
    Long id,
    IncomeCategoryResponse incomeCategory,
    AccountResponse account,
    BigDecimal amount,
    LocalDate date,
    String description
) {
}
