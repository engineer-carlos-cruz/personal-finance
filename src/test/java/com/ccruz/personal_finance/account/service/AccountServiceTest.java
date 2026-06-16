package com.ccruz.personal_finance.account.service;

import com.ccruz.personal_finance.account.persistence.Account;
import com.ccruz.personal_finance.account.persistence.AccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService")
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

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
}
