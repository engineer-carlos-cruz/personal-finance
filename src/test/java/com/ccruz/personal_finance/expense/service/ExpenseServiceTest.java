package com.ccruz.personal_finance.expense.service;

import com.ccruz.personal_finance.account.persistence.Account;
import com.ccruz.personal_finance.expense.persistence.Expense;
import com.ccruz.personal_finance.expense.persistence.ExpenseRepository;
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
@DisplayName("ExpenseService")
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private ExpenseService expenseService;

    @Test
    @DisplayName("findAll should return all expenses")
    void findAll_shouldReturnAllExpenses() {
        var category = ExpenseCategory.builder()
                .id(1L)
                .name("Groceries")
                .build();
        var account = Account.builder()
                .id(1L)
                .code("CASH")
                .description("Cash account")
                .build();
        var expense1 = Expense.builder()
                .id(1L)
                .expenseCategory(category)
                .account(account)
                .amount(new BigDecimal("150.00"))
                .date(LocalDate.of(2026, 6, 1))
                .description("Weekly groceries")
                .build();
        var expense2 = Expense.builder()
                .id(2L)
                .expenseCategory(category)
                .account(account)
                .amount(new BigDecimal("45.00"))
                .date(LocalDate.of(2026, 6, 2))
                .description("Restaurant")
                .build();

        when(expenseRepository.findAll()).thenReturn(List.of(expense1, expense2));

        var result = expenseService.findAll();

        assertThat(result)
                .hasSize(2)
                .containsExactly(expense1, expense2);
    }

    @Test
    @DisplayName("findAll should return empty list when no expenses exist")
    void findAll_shouldReturnEmptyList_whenNoExpensesExist() {
        when(expenseRepository.findAll()).thenReturn(List.of());

        var result = expenseService.findAll();

        assertThat(result).isEmpty();
    }
}
