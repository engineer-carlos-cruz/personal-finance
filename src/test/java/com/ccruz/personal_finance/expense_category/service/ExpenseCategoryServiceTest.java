package com.ccruz.personal_finance.expense_category.service;

import com.ccruz.personal_finance.expense_category.persistence.ExpenseCategory;
import com.ccruz.personal_finance.expense_category.persistence.ExpenseCategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExpenseCategoryService")
class ExpenseCategoryServiceTest {

    @Mock
    private ExpenseCategoryRepository expenseCategoryRepository;

    @InjectMocks
    private ExpenseCategoryService expenseCategoryService;

    @Test
    @DisplayName("findAll should return all active expense categories")
    void findAll_shouldReturnAllActiveCategories() {
        var category1 = ExpenseCategory.builder()
                .id(1L)
                .name("Food")
                .description("Food expenses")
                .isActive(true)
                .build();
        var category2 = ExpenseCategory.builder()
                .id(2L)
                .name("Transport")
                .description("Transport expenses")
                .isActive(true)
                .build();

        when(expenseCategoryRepository.findAll()).thenReturn(List.of(category1, category2));

        var result = expenseCategoryService.findAll();

        assertThat(result)
                .hasSize(2)
                .containsExactly(category1, category2);
    }

    @Test
    @DisplayName("findAll should return empty list when no expense categories exist")
    void findAll_shouldReturnEmptyList_whenNoCategoriesExist() {
        when(expenseCategoryRepository.findAll()).thenReturn(List.of());

        var result = expenseCategoryService.findAll();

        assertThat(result).isEmpty();
    }
}
