package com.ccruz.personal_finance.expense.web;

import com.ccruz.personal_finance.expense.persistence.Expense;
import com.ccruz.personal_finance.expense.web.dto.ExpenseResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ExpenseMapper {

    ExpenseResponse toResponse(Expense expense);

    List<ExpenseResponse> toResponse(List<Expense> expenses);
}
