package com.ccruz.personal_finance.income_category.web.dto;

public record IncomeCategoryResponse(
    Long id,
    String name,
    String description
) {
}
