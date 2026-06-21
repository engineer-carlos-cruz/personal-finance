package com.ccruz.personal_finance.account.service;

import com.ccruz.personal_finance.account.persistence.Account;
import com.ccruz.personal_finance.account.persistence.AccountRepository;
import com.ccruz.personal_finance.account.web.dto.AccountUpsertRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService")
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    // ========== findAll ==========

    @Test
    @DisplayName("findAll should return all active accounts")
    void findAll_shouldReturnAllActiveAccounts() {
        var account1 = Account.builder()
                .id(1L)
                .code("CASH")
                .description("Cash account")
                .balance(new BigDecimal("1000.00"))
                .isActive(true)
                .build();
        var account2 = Account.builder()
                .id(2L)
                .code("BANK")
                .description("Bank account")
                .balance(new BigDecimal("5000.00"))
                .isActive(true)
                .build();

        when(accountRepository.findAll()).thenReturn(List.of(account1, account2));

        var result = accountService.findAll();

        assertThat(result)
                .hasSize(2)
                .containsExactly(account1, account2);
    }

    @Test
    @DisplayName("findAll should return empty list when no accounts exist")
    void findAll_shouldReturnEmptyList_whenNoAccountsExist() {
        when(accountRepository.findAll()).thenReturn(List.of());

        var result = accountService.findAll();

        assertThat(result).isEmpty();
    }

    // ========== findAllIncludingInactive ==========

    @Test
    @DisplayName("findAllIncludingInactive should return all accounts including inactive")
    void findAllIncludingInactive_shouldReturnAllAccountsIncludingInactive() {
        var active = Account.builder()
                .id(1L)
                .code("CASH")
                .description("Cash")
                .balance(BigDecimal.TEN)
                .isActive(true)
                .build();
        var inactive = Account.builder()
                .id(2L)
                .code("CLOSED")
                .description("Closed")
                .balance(BigDecimal.ZERO)
                .isActive(false)
                .build();

        when(accountRepository.findAllIncludingInactive()).thenReturn(List.of(active, inactive));

        var result = accountService.findAllIncludingInactive();

        assertThat(result).hasSize(2).containsExactly(active, inactive);
    }

    // ========== findById ==========

    @Test
    @DisplayName("findById should return account when exists")
    void findById_shouldReturnAccount_whenExists() {
        var account = Account.builder()
                .id(1L)
                .code("CASH")
                .description("Cash")
                .balance(new BigDecimal("1000.00"))
                .isActive(true)
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        var result = accountService.findById(1L);

        assertThat(result).isEqualTo(account);
    }

    @Test
    @DisplayName("findById should throw 404 when not found")
    void findById_shouldThrow404_whenNotFound() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.findById(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Account not found");
    }

    // ========== findByCode ==========

    @Test
    @DisplayName("findByCode should return account when exists")
    void findByCode_shouldReturnAccount_whenExists() {
        var account = Account.builder()
                .id(1L)
                .code("CASH")
                .description("Cash")
                .balance(new BigDecimal("1000.00"))
                .isActive(true)
                .build();

        when(accountRepository.findByCode("CASH")).thenReturn(Optional.of(account));

        var result = accountService.findByCode("CASH");

        assertThat(result).isEqualTo(account);
    }

    @Test
    @DisplayName("findByCode should throw 404 when not found")
    void findByCode_shouldThrow404_whenNotFound() {
        when(accountRepository.findByCode("NONEXISTENT")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.findByCode("NONEXISTENT"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Account not found by code");
    }

    // ========== existsByCode ==========

    @Test
    @DisplayName("existsByCode should return true when code exists")
    void existsByCode_shouldReturnTrue_whenCodeExists() {
        when(accountRepository.existsByCode("CASH")).thenReturn(true);

        var result = accountService.existsByCode("CASH");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("existsByCode should return false when code does not exist")
    void existsByCode_shouldReturnFalse_whenCodeNotExists() {
        when(accountRepository.existsByCode("NONEXISTENT")).thenReturn(false);

        var result = accountService.existsByCode("NONEXISTENT");

        assertThat(result).isFalse();
    }

    // ========== create ==========

    @Test
    @DisplayName("create should persist and return account when code is unique")
    void create_shouldPersistAndReturnAccount_whenCodeIsUnique() {
        var request = new AccountUpsertRequest(" CASH ", " Cash account ", new BigDecimal("1000.00"));

        when(accountRepository.existsByCode(" CASH ")).thenReturn(false);

        var savedAccount = Account.builder()
                .id(1L)
                .code("CASH")
                .description("Cash account")
                .balance(new BigDecimal("1000.00"))
                .isActive(true)
                .build();
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

        var result = accountService.create(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCode()).isEqualTo("CASH");
        assertThat(result.getDescription()).isEqualTo("Cash account");
        assertThat(result.getBalance()).isEqualByComparingTo("1000.00");

        var captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        var captured = captor.getValue();
        assertThat(captured.getCode()).isEqualTo("CASH");
        assertThat(captured.getDescription()).isEqualTo("Cash account");
        assertThat(captured.getBalance()).isEqualByComparingTo("1000.00");
    }

    @Test
    @DisplayName("create should throw 409 when code already exists")
    void create_shouldThrow409_whenCodeAlreadyExists() {
        var request = new AccountUpsertRequest("CASH", "Cash", BigDecimal.TEN);

        when(accountRepository.existsByCode("CASH")).thenReturn(true);

        assertThatThrownBy(() -> accountService.create(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Account code already exists");

        verify(accountRepository).existsByCode("CASH");
        verify(accountRepository, never()).save(any());
    }

    // ========== update ==========

    @Test
    @DisplayName("update should modify and return account when data is valid")
    void update_shouldModifyAndReturnAccount_whenDataIsValid() {
        var existing = Account.builder()
                .id(1L)
                .code("OLD")
                .description("Old desc")
                .balance(BigDecimal.ONE)
                .isActive(true)
                .build();
        var request = new AccountUpsertRequest(" NEW ", " New desc ", new BigDecimal("2000.00"));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(accountRepository.findByCode(" NEW ")).thenReturn(Optional.empty());

        var updatedAccount = Account.builder()
                .id(1L)
                .code("NEW")
                .description("New desc")
                .balance(new BigDecimal("2000.00"))
                .isActive(true)
                .build();
        when(accountRepository.save(any(Account.class))).thenReturn(updatedAccount);

        var result = accountService.update(1L, request);

        assertThat(result.getCode()).isEqualTo("NEW");
        assertThat(result.getDescription()).isEqualTo("New desc");
        assertThat(result.getBalance()).isEqualByComparingTo("2000.00");
    }

    @Test
    @DisplayName("update should throw 409 when code belongs to another account")
    void update_shouldThrow409_whenCodeBelongsToAnotherAccount() {
        var existing = Account.builder()
                .id(1L)
                .code("OLD")
                .description("Old desc")
                .balance(BigDecimal.ONE)
                .isActive(true)
                .build();
        var other = Account.builder()
                .id(2L)
                .code("TAKEN")
                .description("Other account")
                .balance(BigDecimal.TEN)
                .isActive(true)
                .build();
        var request = new AccountUpsertRequest("TAKEN", "New desc", BigDecimal.TEN);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(accountRepository.findByCode("TAKEN")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> accountService.update(1L, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Account code already exists");
    }

    // ========== delete ==========

    @Test
    @DisplayName("delete should remove account when exists")
    void delete_shouldRemoveAccount_whenExists() {
        var account = Account.builder()
                .id(1L)
                .code("CASH")
                .description("Cash")
                .balance(BigDecimal.TEN)
                .isActive(true)
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        accountService.delete(1L);

        verify(accountRepository).delete(account);
    }

    @Test
    @DisplayName("delete should throw 404 when account not found")
    void delete_shouldThrow404_whenNotFound() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.delete(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Account not found");
    }
}
