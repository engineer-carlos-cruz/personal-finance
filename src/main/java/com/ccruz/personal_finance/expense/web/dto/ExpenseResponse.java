package com.ccruz.personal_finance.expense.web.dto;

import com.ccruz.personal_finance.account.web.dto.AccountResponse;
import com.ccruz.personal_finance.expense_category.web.dto.ExpenseCategoryResponse;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseResponse(
    Long id,
    ExpenseCategoryResponse expenseCategory,
    AccountResponse account,
    BigDecimal amount,
    LocalDate date,
    String description
) {
}
