package com.ccruz.personal_finance.income_category.web;

import com.ccruz.personal_finance.income_category.persistence.IncomeCategory;
import com.ccruz.personal_finance.income_category.web.dto.IncomeCategoryResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IncomeCategoryMapper {

    IncomeCategoryResponse toResponse(IncomeCategory incomeCategory);

    List<IncomeCategoryResponse> toResponse(List<IncomeCategory> incomeCategories);
}
