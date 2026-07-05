package com.ccruz.personal_finance.budget.web.dto;

import com.ccruz.personal_finance.budget.persistence.BudgetState;
import com.ccruz.personal_finance.expense_category.web.dto.ExpenseCategoryResponse;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BudgetResponse(
    Long id,
    ExpenseCategoryResponse expenseCategory,
    BigDecimal amount,
    LocalDate initialDate,
    LocalDate finalDate,
    BudgetState state
) {
}
