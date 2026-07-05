package com.ccruz.personal_finance.budget.web;

import com.ccruz.personal_finance.budget.persistence.Budget;
import com.ccruz.personal_finance.budget.web.dto.BudgetResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BudgetMapper {

    BudgetResponse toResponse(Budget budget);

    List<BudgetResponse> toResponse(List<Budget> budgets);
}
