package com.ccruz.personal_finance.income.web;

import com.ccruz.personal_finance.income.persistence.Income;
import com.ccruz.personal_finance.income.web.dto.IncomeResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IncomeMapper {

    IncomeResponse toResponse(Income income);

    List<IncomeResponse> toResponse(List<Income> incomes);
}
