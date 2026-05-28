package com.ccruz.personal_finance.expense_category.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ExpenseCategoryUpsertRequest(
    @NotBlank
    @Size(max = 100)
    String name,

    @Size(max = 255)
    String description
) {
}
