package com.ccruz.personal_finance.expense.service;

import com.ccruz.personal_finance.account.persistence.Account;
import com.ccruz.personal_finance.account.persistence.AccountRepository;
import com.ccruz.personal_finance.expense.persistence.Expense;
import com.ccruz.personal_finance.expense.persistence.ExpenseRepository;
import com.ccruz.personal_finance.expense.web.dto.ExpenseUpsertRequest;
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
@DisplayName("ExpenseService")
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private ExpenseCategoryRepository expenseCategoryRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private ExpenseService expenseService;

    // ========== findAll ==========

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

    // ========== findById ==========

    @Test
    @DisplayName("findById should return expense when exists")
    void findById_shouldReturnExpense_whenExists() {
        var category = ExpenseCategory.builder().id(1L).name("Groceries").build();
        var account = Account.builder().id(1L).code("CASH").description("Cash").build();
        var expense = Expense.builder()
                .id(1L)
                .expenseCategory(category)
                .account(account)
                .amount(new BigDecimal("150.00"))
                .date(LocalDate.of(2026, 6, 1))
                .description("Groceries")
                .build();

        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));

        var result = expenseService.findById(1L);

        assertThat(result).isEqualTo(expense);
    }

    @Test
    @DisplayName("findById should throw 404 when not found")
    void findById_shouldThrow404_whenNotFound() {
        when(expenseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.findById(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Expense not found");
    }

    // ========== create ==========

    @Test
    @DisplayName("create should persist and return expense when category and account exist")
    void create_shouldPersistAndReturnExpense_whenCategoryAndAccountExist() {
        var category = ExpenseCategory.builder().id(10L).name("Groceries").build();
        var account = Account.builder().id(20L).code("CASH").description("Cash").build();
        var request = new ExpenseUpsertRequest(10L, 20L, new BigDecimal("150.00"), LocalDate.of(2026, 6, 1), "Groceries");

        when(expenseCategoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(accountRepository.findById(20L)).thenReturn(Optional.of(account));

        var savedExpense = Expense.builder()
                .id(1L)
                .expenseCategory(category)
                .account(account)
                .amount(new BigDecimal("150.00"))
                .date(LocalDate.of(2026, 6, 1))
                .description("Groceries")
                .build();
        when(expenseRepository.save(any(Expense.class))).thenReturn(savedExpense);

        var result = expenseService.create(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getExpenseCategory()).isEqualTo(category);
        assertThat(result.getAccount()).isEqualTo(account);
        assertThat(result.getAmount()).isEqualByComparingTo("150.00");
        assertThat(result.getDate()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(result.getDescription()).isEqualTo("Groceries");

        var captor = ArgumentCaptor.forClass(Expense.class);
        verify(expenseRepository).save(captor.capture());
        var captured = captor.getValue();
        assertThat(captured.getExpenseCategory()).isEqualTo(category);
        assertThat(captured.getAccount()).isEqualTo(account);
        assertThat(captured.getAmount()).isEqualByComparingTo("150.00");
        assertThat(captured.getDate()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(captured.getDescription()).isEqualTo("Groceries");
    }

    @Test
    @DisplayName("create should throw 404 when expense category not found")
    void create_shouldThrow404_whenExpenseCategoryNotFound() {
        var request = new ExpenseUpsertRequest(99L, 20L, BigDecimal.TEN, LocalDate.now(), "Test");

        when(expenseCategoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.create(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Expense category not found");

        verify(expenseCategoryRepository).findById(99L);
        verify(expenseRepository, never()).save(any());
    }

    @Test
    @DisplayName("create should throw 404 when account not found")
    void create_shouldThrow404_whenAccountNotFound() {
        var category = ExpenseCategory.builder().id(10L).name("Groceries").build();
        var request = new ExpenseUpsertRequest(10L, 99L, BigDecimal.TEN, LocalDate.now(), "Test");

        when(expenseCategoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.create(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Account not found");

        verify(accountRepository).findById(99L);
        verify(expenseRepository, never()).save(any());
    }

    // ========== update ==========

    @Test
    @DisplayName("update should modify and return expense when data is valid")
    void update_shouldModifyAndReturnExpense_whenDataIsValid() {
        var category = ExpenseCategory.builder().id(10L).name("Groceries").build();
        var account = Account.builder().id(20L).code("CASH").description("Cash").build();
        var existing = Expense.builder()
                .id(1L)
                .expenseCategory(category)
                .account(account)
                .amount(new BigDecimal("100.00"))
                .date(LocalDate.of(2026, 5, 1))
                .description("Old")
                .build();
        var request = new ExpenseUpsertRequest(10L, 20L, new BigDecimal("200.00"), LocalDate.of(2026, 6, 1), "Updated");

        when(expenseRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(expenseCategoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(accountRepository.findById(20L)).thenReturn(Optional.of(account));

        var updatedExpense = Expense.builder()
                .id(1L)
                .expenseCategory(category)
                .account(account)
                .amount(new BigDecimal("200.00"))
                .date(LocalDate.of(2026, 6, 1))
                .description("Updated")
                .build();
        when(expenseRepository.save(any(Expense.class))).thenReturn(updatedExpense);

        var result = expenseService.update(1L, request);

        assertThat(result.getAmount()).isEqualByComparingTo("200.00");
        assertThat(result.getDate()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(result.getDescription()).isEqualTo("Updated");
    }

    @Test
    @DisplayName("update should throw 404 when expense not found")
    void update_shouldThrow404_whenExpenseNotFound() {
        var request = new ExpenseUpsertRequest(10L, 20L, BigDecimal.TEN, LocalDate.now(), "Test");

        when(expenseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.update(99L, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Expense not found");

        verify(expenseRepository, never()).save(any());
    }

    @Test
    @DisplayName("update should throw 404 when expense category not found")
    void update_shouldThrow404_whenExpenseCategoryNotFound() {
        var category = ExpenseCategory.builder().id(10L).name("Groceries").build();
        var account = Account.builder().id(20L).code("CASH").description("Cash").build();
        var existing = Expense.builder()
                .id(1L)
                .expenseCategory(category)
                .account(account)
                .amount(BigDecimal.TEN)
                .date(LocalDate.now())
                .description("Existing")
                .build();
        var request = new ExpenseUpsertRequest(99L, 20L, BigDecimal.TEN, LocalDate.now(), "Test");

        when(expenseRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(expenseCategoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.update(1L, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Expense category not found");

        verify(expenseRepository, never()).save(any());
    }

    @Test
    @DisplayName("update should throw 404 when account not found")
    void update_shouldThrow404_whenAccountNotFound() {
        var category = ExpenseCategory.builder().id(10L).name("Groceries").build();
        var account = Account.builder().id(20L).code("CASH").description("Cash").build();
        var existing = Expense.builder()
                .id(1L)
                .expenseCategory(category)
                .account(account)
                .amount(BigDecimal.TEN)
                .date(LocalDate.now())
                .description("Existing")
                .build();
        var request = new ExpenseUpsertRequest(10L, 99L, BigDecimal.TEN, LocalDate.now(), "Test");

        when(expenseRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(expenseCategoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.update(1L, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Account not found");

        verify(expenseRepository, never()).save(any());
    }

    // ========== delete ==========

    @Test
    @DisplayName("delete should remove expense when exists")
    void delete_shouldRemoveExpense_whenExists() {
        var category = ExpenseCategory.builder().id(1L).name("Groceries").build();
        var account = Account.builder().id(1L).code("CASH").description("Cash").build();
        var expense = Expense.builder()
                .id(1L)
                .expenseCategory(category)
                .account(account)
                .amount(BigDecimal.TEN)
                .date(LocalDate.now())
                .description("Test")
                .build();

        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));

        expenseService.delete(1L);

        verify(expenseRepository).delete(expense);
    }

    @Test
    @DisplayName("delete should throw 404 when expense not found")
    void delete_shouldThrow404_whenNotFound() {
        when(expenseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.delete(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Expense not found");

        verify(expenseRepository, never()).delete(any());
    }
}
