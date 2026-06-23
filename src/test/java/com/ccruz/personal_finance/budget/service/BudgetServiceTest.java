package com.ccruz.personal_finance.budget.service;

import com.ccruz.personal_finance.budget.persistence.Budget;
import com.ccruz.personal_finance.budget.persistence.BudgetRepository;
import com.ccruz.personal_finance.budget.persistence.BudgetState;
import com.ccruz.personal_finance.budget.web.dto.BudgetUpsertRequest;
import com.ccruz.personal_finance.expense_category.persistence.ExpenseCategory;
import com.ccruz.personal_finance.expense_category.persistence.ExpenseCategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BudgetService")
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private ExpenseCategoryRepository expenseCategoryRepository;

    @InjectMocks
    private BudgetService budgetService;

    // ========== findAll ==========

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

    // ========== findAllIncludingInactive ==========

    @Test
    @DisplayName("findAllIncludingInactive should return all budgets including inactive")
    void findAllIncludingInactive_shouldReturnAllBudgetsIncludingInactive() {
        var category = ExpenseCategory.builder()
                .id(1L)
                .name("Food")
                .build();
        var activeBudget = Budget.builder()
                .id(1L)
                .expenseCategory(category)
                .amount(new BigDecimal("500.00"))
                .initialDate(LocalDate.of(2026, 1, 1))
                .finalDate(LocalDate.of(2026, 1, 31))
                .state(BudgetState.WITHIN)
                .isActive(true)
                .build();
        var inactiveBudget = Budget.builder()
                .id(2L)
                .expenseCategory(category)
                .amount(new BigDecimal("1000.00"))
                .initialDate(LocalDate.of(2026, 2, 1))
                .finalDate(LocalDate.of(2026, 2, 28))
                .state(BudgetState.FULLY_USED)
                .isActive(false)
                .build();

        when(budgetRepository.findAllIncludingInactive()).thenReturn(List.of(activeBudget, inactiveBudget));

        var result = budgetService.findAllIncludingInactive();

        assertThat(result)
                .hasSize(2)
                .containsExactly(activeBudget, inactiveBudget);
    }

    @Test
    @DisplayName("findAllIncludingInactive should return empty list when no budgets exist")
    void findAllIncludingInactive_shouldReturnEmptyList_whenNoBudgetsExist() {
        when(budgetRepository.findAllIncludingInactive()).thenReturn(List.of());

        var result = budgetService.findAllIncludingInactive();

        assertThat(result).isEmpty();
    }

    // ========== findById ==========

    @Test
    @DisplayName("findById should return budget when exists")
    void findById_shouldReturnBudget_whenExists() {
        var category = ExpenseCategory.builder().id(1L).name("Food").build();
        var budget = Budget.builder()
                .id(1L)
                .expenseCategory(category)
                .amount(new BigDecimal("500.00"))
                .initialDate(LocalDate.of(2026, 1, 1))
                .finalDate(LocalDate.of(2026, 1, 31))
                .state(BudgetState.NOT_STARTED)
                .isActive(true)
                .build();

        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budget));

        var result = budgetService.findById(1L);

        assertThat(result).isEqualTo(budget);
    }

    @Test
    @DisplayName("findById should throw 404 when not found")
    void findById_shouldThrow404_whenNotFound() {
        when(budgetRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.findById(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Budget not found");
    }

    // ========== create ==========

    @Test
    @DisplayName("create should persist and return budget when category exists")
    void create_shouldPersistAndReturnBudget_whenCategoryExists() {
        var category = ExpenseCategory.builder().id(10L).name("Food").build();
        var request = new BudgetUpsertRequest(10L, new BigDecimal("500.00"), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        when(expenseCategoryRepository.findById(10L)).thenReturn(Optional.of(category));

        var savedBudget = Budget.builder()
                .id(1L)
                .expenseCategory(category)
                .amount(new BigDecimal("500.00"))
                .initialDate(LocalDate.of(2026, 1, 1))
                .finalDate(LocalDate.of(2026, 1, 31))
                .state(BudgetState.NOT_STARTED)
                .isActive(true)
                .build();
        when(budgetRepository.save(any(Budget.class))).thenReturn(savedBudget);

        var result = budgetService.create(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getExpenseCategory()).isEqualTo(category);
        assertThat(result.getAmount()).isEqualByComparingTo("500.00");
        assertThat(result.getInitialDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(result.getFinalDate()).isEqualTo(LocalDate.of(2026, 1, 31));
        assertThat(result.getState()).isEqualTo(BudgetState.NOT_STARTED);
        assertThat(result.getIsActive()).isTrue();

        var captor = ArgumentCaptor.forClass(Budget.class);
        verify(budgetRepository).save(captor.capture());
        var captured = captor.getValue();
        assertThat(captured.getExpenseCategory()).isEqualTo(category);
        assertThat(captured.getAmount()).isEqualByComparingTo("500.00");
        assertThat(captured.getInitialDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(captured.getFinalDate()).isEqualTo(LocalDate.of(2026, 1, 31));
        assertThat(captured.getState()).isEqualTo(BudgetState.NOT_STARTED);
        assertThat(captured.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("create should throw 404 when expense category not found")
    void create_shouldThrow404_whenExpenseCategoryNotFound() {
        var request = new BudgetUpsertRequest(99L, BigDecimal.TEN, LocalDate.now(), LocalDate.now().plusDays(30));

        when(expenseCategoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.create(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Expense category not found");

        verify(expenseCategoryRepository).findById(99L);
        verify(budgetRepository, never()).save(any());
    }

    // ========== update ==========

    @Test
    @DisplayName("update should modify and return budget when data is valid")
    void update_shouldModifyAndReturnBudget_whenDataIsValid() {
        var category = ExpenseCategory.builder().id(10L).name("Food").build();
        var existing = Budget.builder()
                .id(1L)
                .expenseCategory(category)
                .amount(new BigDecimal("100.00"))
                .initialDate(LocalDate.of(2026, 1, 1))
                .finalDate(LocalDate.of(2026, 1, 15))
                .state(BudgetState.NOT_STARTED)
                .isActive(true)
                .build();
        var request = new BudgetUpsertRequest(10L, new BigDecimal("200.00"), LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28));

        when(budgetRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(expenseCategoryRepository.findById(10L)).thenReturn(Optional.of(category));

        var updatedBudget = Budget.builder()
                .id(1L)
                .expenseCategory(category)
                .amount(new BigDecimal("200.00"))
                .initialDate(LocalDate.of(2026, 2, 1))
                .finalDate(LocalDate.of(2026, 2, 28))
                .state(BudgetState.NOT_STARTED)
                .isActive(true)
                .build();
        when(budgetRepository.save(any(Budget.class))).thenReturn(updatedBudget);

        var result = budgetService.update(1L, request);

        assertThat(result.getAmount()).isEqualByComparingTo("200.00");
        assertThat(result.getInitialDate()).isEqualTo(LocalDate.of(2026, 2, 1));
        assertThat(result.getFinalDate()).isEqualTo(LocalDate.of(2026, 2, 28));
    }

    @Test
    @DisplayName("update should throw 404 when budget not found")
    void update_shouldThrow404_whenBudgetNotFound() {
        var request = new BudgetUpsertRequest(10L, BigDecimal.TEN, LocalDate.now(), LocalDate.now().plusDays(30));

        when(budgetRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.update(99L, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Budget not found");

        verify(budgetRepository, never()).save(any());
    }

    @Test
    @DisplayName("update should throw 404 when expense category not found")
    void update_shouldThrow404_whenExpenseCategoryNotFound() {
        var category = ExpenseCategory.builder().id(10L).name("Food").build();
        var existing = Budget.builder()
                .id(1L)
                .expenseCategory(category)
                .amount(BigDecimal.TEN)
                .initialDate(LocalDate.now())
                .finalDate(LocalDate.now().plusDays(30))
                .state(BudgetState.NOT_STARTED)
                .isActive(true)
                .build();
        var request = new BudgetUpsertRequest(99L, BigDecimal.TEN, LocalDate.now(), LocalDate.now().plusDays(30));

        when(budgetRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(expenseCategoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.update(1L, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Expense category not found");

        verify(budgetRepository, never()).save(any());
    }

    // ========== delete ==========

    @Test
    @DisplayName("delete should remove budget when exists")
    void delete_shouldRemoveBudget_whenExists() {
        var category = ExpenseCategory.builder().id(1L).name("Food").build();
        var budget = Budget.builder()
                .id(1L)
                .expenseCategory(category)
                .amount(BigDecimal.TEN)
                .initialDate(LocalDate.now())
                .finalDate(LocalDate.now().plusDays(30))
                .state(BudgetState.NOT_STARTED)
                .isActive(true)
                .build();

        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budget));

        budgetService.delete(1L);

        verify(budgetRepository).delete(budget);
    }

    @Test
    @DisplayName("delete should throw 404 when budget not found")
    void delete_shouldThrow404_whenNotFound() {
        when(budgetRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.delete(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Budget not found");

        verify(budgetRepository, never()).delete(any());
    }
}
