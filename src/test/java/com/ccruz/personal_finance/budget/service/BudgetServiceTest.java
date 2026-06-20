package com.ccruz.personal_finance.budget.service;

import com.ccruz.personal_finance.budget.persistence.Budget;
import com.ccruz.personal_finance.budget.persistence.BudgetRepository;
import com.ccruz.personal_finance.budget.persistence.BudgetState;
import com.ccruz.personal_finance.expense_category.persistence.ExpenseCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BudgetService")
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @InjectMocks
    private BudgetService budgetService;

    @Test
    @DisplayName("findAll should return all active budgets")
    void findAll_shouldReturnAllActiveBudgets() {
        var category = ExpenseCategory.builder()
                .id(1L)
                .name("Food")
                .build();
        var budget1 = Budget.builder()
                .id(1L)
                .expenseCategory(category)
                .amount(new BigDecimal("500.00"))
                .initialDate(LocalDate.of(2026, 1, 1))
                .finalDate(LocalDate.of(2026, 1, 31))
                .state(BudgetState.NOT_STARTED)
                .isActive(true)
                .build();
        var budget2 = Budget.builder()
                .id(2L)
                .expenseCategory(category)
                .amount(new BigDecimal("1000.00"))
                .initialDate(LocalDate.of(2026, 2, 1))
                .finalDate(LocalDate.of(2026, 2, 28))
                .state(BudgetState.WITHIN)
                .isActive(true)
                .build();

        when(budgetRepository.findAll()).thenReturn(List.of(budget1, budget2));

        var result = budgetService.findAll();

        assertThat(result)
                .hasSize(2)
                .containsExactly(budget1, budget2);
    }

    @Test
    @DisplayName("findAll should return empty list when no budgets exist")
    void findAll_shouldReturnEmptyList_whenNoBudgetsExist() {
        when(budgetRepository.findAll()).thenReturn(List.of());

        var result = budgetService.findAll();

        assertThat(result).isEmpty();
    }
}
