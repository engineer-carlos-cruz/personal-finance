package com.ccruz.personal_finance.transfer.service;

import com.ccruz.personal_finance.account.persistence.Account;
import com.ccruz.personal_finance.account.persistence.AccountRepository;
import com.ccruz.personal_finance.transfer.persistence.Transfer;
import com.ccruz.personal_finance.transfer.persistence.TransferRepository;
import com.ccruz.personal_finance.transfer.web.dto.TransferUpsertRequest;
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
@DisplayName("TransferService")
class TransferServiceTest {

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransferService transferService;

    // ========== findAll ==========

    @Test
    @DisplayName("findAll should return all transfers")
    void findAll_shouldReturnAllTransfers() {
        var source = Account.builder().id(1L).code("CASH").description("Cash").build();
        var target = Account.builder().id(2L).code("BANK").description("Bank").build();
        var transfer = Transfer.builder()
                .id(1L)
                .sourceAccount(source)
                .targetAccount(target)
                .amount(new BigDecimal("100.00"))
                .date(LocalDate.of(2026, 7, 9))
                .build();

        when(transferRepository.findAll()).thenReturn(List.of(transfer));

        var result = transferService.findAll();

        assertThat(result)
                .hasSize(1)
                .containsExactly(transfer);
    }

    @Test
    @DisplayName("findAll should return empty list when no transfers exist")
    void findAll_shouldReturnEmptyList_whenNoTransfersExist() {
        when(transferRepository.findAll()).thenReturn(List.of());

        var result = transferService.findAll();

        assertThat(result).isEmpty();
    }

    // ========== findById ==========

    @Test
    @DisplayName("findById should return transfer when exists")
    void findById_shouldReturnTransfer_whenExists() {
        var source = Account.builder().id(1L).code("CASH").build();
        var target = Account.builder().id(2L).code("BANK").build();
        var transfer = Transfer.builder()
                .id(1L)
                .sourceAccount(source)
                .targetAccount(target)
                .amount(new BigDecimal("100.00"))
                .date(LocalDate.of(2026, 7, 9))
                .build();

        when(transferRepository.findById(1L)).thenReturn(Optional.of(transfer));

        var result = transferService.findById(1L);

        assertThat(result).isEqualTo(transfer);
    }

    @Test
    @DisplayName("findById should throw 404 when not found")
    void findById_shouldThrow404_whenNotFound() {
        when(transferRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferService.findById(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Transfer not found");
    }

    // ========== create ==========

    @Test
    @DisplayName("create should persist and return transfer when data is valid")
    void create_shouldPersistAndReturnTransfer_whenDataIsValid() {
        var request = new TransferUpsertRequest(1L, 2L, new BigDecimal("100.00"), LocalDate.of(2026, 7, 9));
        var source = Account.builder().id(1L).code("CASH").description("Cash").build();
        var target = Account.builder().id(2L).code("BANK").description("Bank").build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(source));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(target));

        var savedTransfer = Transfer.builder()
                .id(1L)
                .sourceAccount(source)
                .targetAccount(target)
                .amount(new BigDecimal("100.00"))
                .date(LocalDate.of(2026, 7, 9))
                .build();
        when(transferRepository.save(any(Transfer.class))).thenReturn(savedTransfer);

        var result = transferService.create(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getSourceAccount().getId()).isEqualTo(1L);
        assertThat(result.getTargetAccount().getId()).isEqualTo(2L);
        assertThat(result.getAmount()).isEqualByComparingTo("100.00");
        assertThat(result.getDate()).isEqualTo(LocalDate.of(2026, 7, 9));

        var captor = ArgumentCaptor.forClass(Transfer.class);
        verify(transferRepository).save(captor.capture());
        var captured = captor.getValue();
        assertThat(captured.getSourceAccount().getId()).isEqualTo(1L);
        assertThat(captured.getTargetAccount().getId()).isEqualTo(2L);
        assertThat(captured.getAmount()).isEqualByComparingTo("100.00");
        assertThat(captured.getDate()).isEqualTo(LocalDate.of(2026, 7, 9));
    }

    @Test
    @DisplayName("create should throw 404 when source account not found")
    void create_shouldThrow404_whenSourceAccountNotFound() {
        var request = new TransferUpsertRequest(99L, 2L, BigDecimal.TEN, LocalDate.of(2026, 7, 9));

        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferService.create(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Source account not found");

        verify(accountRepository).findById(99L);
        verify(transferRepository, never()).save(any());
    }

    @Test
    @DisplayName("create should throw 404 when target account not found")
    void create_shouldThrow404_whenTargetAccountNotFound() {
        var request = new TransferUpsertRequest(1L, 99L, BigDecimal.TEN, LocalDate.of(2026, 7, 9));
        var source = Account.builder().id(1L).code("CASH").build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(source));
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferService.create(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Target account not found");

        verify(transferRepository, never()).save(any());
    }

    @Test
    @DisplayName("create should throw 400 when source and target are the same")
    void create_shouldThrow400_whenSourceAndTargetAreSame() {
        var request = new TransferUpsertRequest(1L, 1L, BigDecimal.TEN, LocalDate.of(2026, 7, 9));
        var account = Account.builder().id(1L).code("CASH").build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> transferService.create(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Source and target accounts must be different");

        verify(transferRepository, never()).save(any());
    }

    // ========== update ==========

    @Test
    @DisplayName("update should modify and return transfer when data is valid")
    void update_shouldModifyAndReturnTransfer_whenDataIsValid() {
        var source = Account.builder().id(1L).code("CASH").description("Cash").build();
        var target = Account.builder().id(2L).code("BANK").description("Bank").build();
        var existing = Transfer.builder()
                .id(1L)
                .sourceAccount(source)
                .targetAccount(target)
                .amount(new BigDecimal("50.00"))
                .date(LocalDate.of(2026, 7, 1))
                .build();

        var request = new TransferUpsertRequest(2L, 1L, new BigDecimal("200.00"), LocalDate.of(2026, 7, 9));

        when(transferRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(Account.builder().id(2L).code("BANK").build()));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(Account.builder().id(1L).code("CASH").build()));

        var updatedTransfer = Transfer.builder()
                .id(1L)
                .sourceAccount(Account.builder().id(2L).code("BANK").build())
                .targetAccount(Account.builder().id(1L).code("CASH").build())
                .amount(new BigDecimal("200.00"))
                .date(LocalDate.of(2026, 7, 9))
                .build();
        when(transferRepository.save(any(Transfer.class))).thenReturn(updatedTransfer);

        var result = transferService.update(1L, request);

        assertThat(result.getSourceAccount().getId()).isEqualTo(2L);
        assertThat(result.getTargetAccount().getId()).isEqualTo(1L);
        assertThat(result.getAmount()).isEqualByComparingTo("200.00");
        assertThat(result.getDate()).isEqualTo(LocalDate.of(2026, 7, 9));
    }

    @Test
    @DisplayName("update should throw 404 when transfer not found")
    void update_shouldThrow404_whenTransferNotFound() {
        var request = new TransferUpsertRequest(1L, 2L, BigDecimal.TEN, LocalDate.of(2026, 7, 9));

        when(transferRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferService.update(99L, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Transfer not found");

        verify(transferRepository).findById(99L);
        verify(transferRepository, never()).save(any());
    }

    @Test
    @DisplayName("update should throw 400 when source and target are the same")
    void update_shouldThrow400_whenSourceAndTargetAreSame() {
        var source = Account.builder().id(1L).code("CASH").build();
        var target = Account.builder().id(2L).code("BANK").build();
        var existing = Transfer.builder()
                .id(1L)
                .sourceAccount(source)
                .targetAccount(target)
                .amount(BigDecimal.TEN)
                .date(LocalDate.of(2026, 7, 9))
                .build();
        var request = new TransferUpsertRequest(1L, 1L, BigDecimal.TEN, LocalDate.of(2026, 7, 9));

        when(transferRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> transferService.update(1L, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Source and target accounts must be different");

        verify(transferRepository, never()).save(any());
    }

    @Test
    @DisplayName("update should throw 404 when source account not found")
    void update_shouldThrow404_whenSourceAccountNotFound() {
        var source = Account.builder().id(1L).code("CASH").build();
        var target = Account.builder().id(2L).code("BANK").build();
        var existing = Transfer.builder()
                .id(1L)
                .sourceAccount(source)
                .targetAccount(target)
                .amount(BigDecimal.TEN)
                .date(LocalDate.of(2026, 7, 9))
                .build();
        var request = new TransferUpsertRequest(99L, 2L, BigDecimal.TEN, LocalDate.of(2026, 7, 9));

        when(transferRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferService.update(1L, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Source account not found");

        verify(transferRepository, never()).save(any());
    }

    @Test
    @DisplayName("update should throw 404 when target account not found")
    void update_shouldThrow404_whenTargetAccountNotFound() {
        var source = Account.builder().id(1L).code("CASH").build();
        var target = Account.builder().id(2L).code("BANK").build();
        var existing = Transfer.builder()
                .id(1L)
                .sourceAccount(source)
                .targetAccount(target)
                .amount(BigDecimal.TEN)
                .date(LocalDate.of(2026, 7, 9))
                .build();
        var request = new TransferUpsertRequest(1L, 99L, BigDecimal.TEN, LocalDate.of(2026, 7, 9));

        when(transferRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(source));
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferService.update(1L, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Target account not found");

        verify(transferRepository, never()).save(any());
    }

    // ========== delete ==========

    @Test
    @DisplayName("delete should remove transfer when exists")
    void delete_shouldRemoveTransfer_whenExists() {
        var source = Account.builder().id(1L).code("CASH").build();
        var target = Account.builder().id(2L).code("BANK").build();
        var transfer = Transfer.builder()
                .id(1L)
                .sourceAccount(source)
                .targetAccount(target)
                .amount(BigDecimal.TEN)
                .date(LocalDate.of(2026, 7, 9))
                .build();

        when(transferRepository.findById(1L)).thenReturn(Optional.of(transfer));

        transferService.delete(1L);

        verify(transferRepository).delete(transfer);
    }

    @Test
    @DisplayName("delete should throw 404 when transfer not found")
    void delete_shouldThrow404_whenNotFound() {
        when(transferRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferService.delete(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Transfer not found");
    }
}
