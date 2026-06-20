package com.ccruz.personal_finance.income.service;

import com.ccruz.personal_finance.account.persistence.Account;
import com.ccruz.personal_finance.income.persistence.Income;
import com.ccruz.personal_finance.income.persistence.IncomeRepository;
import com.ccruz.personal_finance.income_category.persistence.IncomeCategory;
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
@DisplayName("IncomeService")
class IncomeServiceTest {

    @Mock
    private IncomeRepository incomeRepository;

    @InjectMocks
    private IncomeService incomeService;

    @Test
    @DisplayName("findAll should return all incomes")
    void findAll_shouldReturnAllIncomes() {
        var category = IncomeCategory.builder()
                .id(1L)
                .name("Salary")
                .build();
        var account = Account.builder()
                .id(1L)
                .code("CASH")
                .description("Cash account")
                .build();
        var income1 = Income.builder()
                .id(1L)
                .incomeCategory(category)
                .account(account)
                .amount(new BigDecimal("3000.00"))
                .date(LocalDate.of(2026, 6, 1))
                .description("Monthly salary")
                .build();
        var income2 = Income.builder()
                .id(2L)
                .incomeCategory(category)
                .account(account)
                .amount(new BigDecimal("500.00"))
                .date(LocalDate.of(2026, 6, 15))
                .description("Freelance")
                .build();

        when(incomeRepository.findAll()).thenReturn(List.of(income1, income2));

        var result = incomeService.findAll();

        assertThat(result)
                .hasSize(2)
                .containsExactly(income1, income2);
    }

    @Test
    @DisplayName("findAll should return empty list when no incomes exist")
    void findAll_shouldReturnEmptyList_whenNoIncomesExist() {
        when(incomeRepository.findAll()).thenReturn(List.of());

        var result = incomeService.findAll();

        assertThat(result).isEmpty();
    }
}
