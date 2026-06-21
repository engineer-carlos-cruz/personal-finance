package com.ccruz.personal_finance.income.service;

import com.ccruz.personal_finance.account.persistence.Account;
import com.ccruz.personal_finance.account.persistence.AccountRepository;
import com.ccruz.personal_finance.income.persistence.Income;
import com.ccruz.personal_finance.income.persistence.IncomeRepository;
import com.ccruz.personal_finance.income.web.dto.IncomeUpsertRequest;
import com.ccruz.personal_finance.income_category.persistence.IncomeCategory;
import com.ccruz.personal_finance.income_category.persistence.IncomeCategoryRepository;
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
@DisplayName("IncomeService")
class IncomeServiceTest {

    @Mock
    private IncomeRepository incomeRepository;

    @Mock
    private IncomeCategoryRepository incomeCategoryRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private IncomeService incomeService;

    // ========== findAll ==========

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

    // ========== findById ==========

    @Test
    @DisplayName("findById should return income when exists")
    void findById_shouldReturnIncome_whenExists() {
        var category = IncomeCategory.builder().id(1L).name("Salary").build();
        var account = Account.builder().id(1L).code("CASH").description("Cash").build();
        var income = Income.builder()
                .id(1L)
                .incomeCategory(category)
                .account(account)
                .amount(new BigDecimal("3000.00"))
                .date(LocalDate.of(2026, 6, 1))
                .description("Salary")
                .build();

        when(incomeRepository.findById(1L)).thenReturn(Optional.of(income));

        var result = incomeService.findById(1L);

        assertThat(result).isEqualTo(income);
    }

    @Test
    @DisplayName("findById should throw 404 when not found")
    void findById_shouldThrow404_whenNotFound() {
        when(incomeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> incomeService.findById(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Income not found");
    }

    // ========== create ==========

    @Test
    @DisplayName("create should persist and return income when category and account exist")
    void create_shouldPersistAndReturnIncome_whenCategoryAndAccountExist() {
        var category = IncomeCategory.builder().id(10L).name("Salary").build();
        var account = Account.builder().id(20L).code("CASH").description("Cash").build();
        var request = new IncomeUpsertRequest(10L, 20L, new BigDecimal("3000.00"), LocalDate.of(2026, 6, 1), "Monthly salary");

        when(incomeCategoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(accountRepository.findById(20L)).thenReturn(Optional.of(account));

        var savedIncome = Income.builder()
                .id(1L)
                .incomeCategory(category)
                .account(account)
                .amount(new BigDecimal("3000.00"))
                .date(LocalDate.of(2026, 6, 1))
                .description("Monthly salary")
                .build();
        when(incomeRepository.save(any(Income.class))).thenReturn(savedIncome);

        var result = incomeService.create(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getIncomeCategory()).isEqualTo(category);
        assertThat(result.getAccount()).isEqualTo(account);
        assertThat(result.getAmount()).isEqualByComparingTo("3000.00");
        assertThat(result.getDate()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(result.getDescription()).isEqualTo("Monthly salary");

        var captor = ArgumentCaptor.forClass(Income.class);
        verify(incomeRepository).save(captor.capture());
        var captured = captor.getValue();
        assertThat(captured.getIncomeCategory()).isEqualTo(category);
        assertThat(captured.getAccount()).isEqualTo(account);
        assertThat(captured.getAmount()).isEqualByComparingTo("3000.00");
        assertThat(captured.getDate()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(captured.getDescription()).isEqualTo("Monthly salary");
    }

    @Test
    @DisplayName("create should throw 404 when income category not found")
    void create_shouldThrow404_whenIncomeCategoryNotFound() {
        var request = new IncomeUpsertRequest(99L, 20L, BigDecimal.TEN, LocalDate.now(), "Test");

        when(incomeCategoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> incomeService.create(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Income category not found");

        verify(incomeCategoryRepository).findById(99L);
        verify(incomeRepository, never()).save(any());
    }

    @Test
    @DisplayName("create should throw 404 when account not found")
    void create_shouldThrow404_whenAccountNotFound() {
        var category = IncomeCategory.builder().id(10L).name("Salary").build();
        var request = new IncomeUpsertRequest(10L, 99L, BigDecimal.TEN, LocalDate.now(), "Test");

        when(incomeCategoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> incomeService.create(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Account not found");

        verify(accountRepository).findById(99L);
        verify(incomeRepository, never()).save(any());
    }

    // ========== update ==========

    @Test
    @DisplayName("update should modify and return income when data is valid")
    void update_shouldModifyAndReturnIncome_whenDataIsValid() {
        var category = IncomeCategory.builder().id(10L).name("Salary").build();
        var account = Account.builder().id(20L).code("CASH").description("Cash").build();
        var existing = Income.builder()
                .id(1L)
                .incomeCategory(category)
                .account(account)
                .amount(new BigDecimal("1000.00"))
                .date(LocalDate.of(2026, 5, 1))
                .description("Old")
                .build();
        var request = new IncomeUpsertRequest(10L, 20L, new BigDecimal("3000.00"), LocalDate.of(2026, 6, 1), "Updated");

        when(incomeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(incomeCategoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(accountRepository.findById(20L)).thenReturn(Optional.of(account));

        var updatedIncome = Income.builder()
                .id(1L)
                .incomeCategory(category)
                .account(account)
                .amount(new BigDecimal("3000.00"))
                .date(LocalDate.of(2026, 6, 1))
                .description("Updated")
                .build();
        when(incomeRepository.save(any(Income.class))).thenReturn(updatedIncome);

        var result = incomeService.update(1L, request);

        assertThat(result.getAmount()).isEqualByComparingTo("3000.00");
        assertThat(result.getDate()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(result.getDescription()).isEqualTo("Updated");
    }

    @Test
    @DisplayName("update should throw 404 when income not found")
    void update_shouldThrow404_whenIncomeNotFound() {
        var request = new IncomeUpsertRequest(10L, 20L, BigDecimal.TEN, LocalDate.now(), "Test");

        when(incomeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> incomeService.update(99L, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Income not found");

        verify(incomeRepository, never()).save(any());
    }

    @Test
    @DisplayName("update should throw 404 when income category not found")
    void update_shouldThrow404_whenIncomeCategoryNotFound() {
        var category = IncomeCategory.builder().id(10L).name("Salary").build();
        var account = Account.builder().id(20L).code("CASH").description("Cash").build();
        var existing = Income.builder()
                .id(1L)
                .incomeCategory(category)
                .account(account)
                .amount(BigDecimal.TEN)
                .date(LocalDate.now())
                .description("Existing")
                .build();
        var request = new IncomeUpsertRequest(99L, 20L, BigDecimal.TEN, LocalDate.now(), "Test");

        when(incomeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(incomeCategoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> incomeService.update(1L, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Income category not found");

        verify(incomeRepository, never()).save(any());
    }

    @Test
    @DisplayName("update should throw 404 when account not found")
    void update_shouldThrow404_whenAccountNotFound() {
        var category = IncomeCategory.builder().id(10L).name("Salary").build();
        var account = Account.builder().id(20L).code("CASH").description("Cash").build();
        var existing = Income.builder()
                .id(1L)
                .incomeCategory(category)
                .account(account)
                .amount(BigDecimal.TEN)
                .date(LocalDate.now())
                .description("Existing")
                .build();
        var request = new IncomeUpsertRequest(10L, 99L, BigDecimal.TEN, LocalDate.now(), "Test");

        when(incomeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(incomeCategoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> incomeService.update(1L, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Account not found");

        verify(incomeRepository, never()).save(any());
    }

    // ========== delete ==========

    @Test
    @DisplayName("delete should remove income when exists")
    void delete_shouldRemoveIncome_whenExists() {
        var category = IncomeCategory.builder().id(1L).name("Salary").build();
        var account = Account.builder().id(1L).code("CASH").description("Cash").build();
        var income = Income.builder()
                .id(1L)
                .incomeCategory(category)
                .account(account)
                .amount(BigDecimal.TEN)
                .date(LocalDate.now())
                .description("Test")
                .build();

        when(incomeRepository.findById(1L)).thenReturn(Optional.of(income));

        incomeService.delete(1L);

        verify(incomeRepository).delete(income);
    }

    @Test
    @DisplayName("delete should throw 404 when income not found")
    void delete_shouldThrow404_whenNotFound() {
        when(incomeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> incomeService.delete(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Income not found");

        verify(incomeRepository, never()).delete(any());
    }
}
