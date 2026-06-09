package com.ccruz.personal_finance.expense_category.web.dto;

public record ExpenseCategoryResponse(
    Long id,
    String name,
    String description
) {
}
