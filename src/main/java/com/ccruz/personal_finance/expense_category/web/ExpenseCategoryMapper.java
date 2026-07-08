package com.ccruz.personal_finance.expense_category.web;

import com.ccruz.personal_finance.expense_category.persistence.ExpenseCategory;
import com.ccruz.personal_finance.expense_category.web.dto.ExpenseCategoryResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ExpenseCategoryMapper {

    ExpenseCategoryResponse toResponse(ExpenseCategory expenseCategory);

    List<ExpenseCategoryResponse> toResponse(List<ExpenseCategory> expenseCategories);
}
