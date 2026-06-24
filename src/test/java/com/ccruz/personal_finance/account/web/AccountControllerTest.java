package com.ccruz.personal_finance.account.web;

import com.ccruz.personal_finance.account.persistence.Account;
import com.ccruz.personal_finance.account.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountController - Standalone")
class AccountControllerTest {

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountController accountController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();
    }

    @Test
    @DisplayName("findAll should return 200 with list of accounts")
    void findAll_shouldReturn200WithAccounts() throws Exception {
        var cash = Account.builder()
                .id(1L)
                .code("CASH")
                .description("Cash account")
                .balance(new BigDecimal("1000.00"))
                .isActive(true)
                .build();
        var bank = Account.builder()
                .id(2L)
                .code("BANK")
                .description("Bank account")
                .balance(new BigDecimal("5000.00"))
                .isActive(true)
                .build();

        when(accountService.findAll()).thenReturn(List.of(cash, bank));

        mockMvc.perform(get("/api/accounts").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].code").value("CASH"))
                .andExpect(jsonPath("$[0].description").value("Cash account"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].code").value("BANK"));

        verify(accountService).findAll();
    }

    @Test
    @DisplayName("findAll should return 200 with empty list when no accounts exist")
    void findAll_shouldReturn200WithEmptyList() throws Exception {
        when(accountService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/accounts").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));

        verify(accountService).findAll();
    }

    @Test
    @DisplayName("findAllIncludingInactive should return 200 with list of accounts")
    void findAllIncludingInactive_shouldReturn200WithAccounts() throws Exception {
        var cash = Account.builder()
                .id(1L)
                .code("CASH")
                .description("Cash account")
                .balance(new BigDecimal("1000.00"))
                .isActive(true)
                .build();
        var bank = Account.builder()
                .id(2L)
                .code("BANK")
                .description("Bank account")
                .balance(new BigDecimal("5000.00"))
                .isActive(false)
                .build();

        when(accountService.findAllIncludingInactive()).thenReturn(List.of(cash, bank));

        mockMvc.perform(get("/api/accounts/with-inactive").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(accountService).findAllIncludingInactive();
    }

    @Test
    @DisplayName("findAllIncludingInactive should return 200 with empty list")
    void findAllIncludingInactive_shouldReturn200WithEmptyList() throws Exception {
        when(accountService.findAllIncludingInactive()).thenReturn(List.of());

        mockMvc.perform(get("/api/accounts/with-inactive").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));

        verify(accountService).findAllIncludingInactive();
    }

    @Test
    @DisplayName("findById should return 200 with account when found")
    void findById_shouldReturn200WhenFound() throws Exception {
        var account = Account.builder()
                .id(1L)
                .code("CASH")
                .description("Cash account")
                .balance(new BigDecimal("1000.00"))
                .isActive(true)
                .build();

        when(accountService.findById(1L)).thenReturn(account);

        mockMvc.perform(get("/api/accounts/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("CASH"));

        verify(accountService).findById(1L);
    }

    @Test
    @DisplayName("findById should return 404 when not found")
    void findById_shouldReturn404WhenNotFound() throws Exception {
        when(accountService.findById(anyLong()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        mockMvc.perform(get("/api/accounts/99").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(accountService).findById(99L);
    }

    @Test
    @DisplayName("findByCode should return 200 with account when found")
    void findByCode_shouldReturn200WhenFound() throws Exception {
        var account = Account.builder()
                .id(1L)
                .code("CASH")
                .description("Cash account")
                .balance(new BigDecimal("1000.00"))
                .isActive(true)
                .build();

        when(accountService.findByCode("CASH")).thenReturn(account);

        mockMvc.perform(get("/api/accounts/by-code/CASH").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("CASH"));

        verify(accountService).findByCode("CASH");
    }

    @Test
    @DisplayName("findByCode should return 404 when not found")
    void findByCode_shouldReturn404WhenNotFound() throws Exception {
        when(accountService.findByCode(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found by code"));

        mockMvc.perform(get("/api/accounts/by-code/NONEXISTENT").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(accountService).findByCode("NONEXISTENT");
    }

    @Test
    @DisplayName("existsByCode should return 200 with true when code exists")
    void existsByCode_shouldReturn200WithTrueWhenExists() throws Exception {
        when(accountService.existsByCode("CASH")).thenReturn(true);

        mockMvc.perform(get("/api/accounts/by-code/CASH/exists").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(accountService).existsByCode("CASH");
    }

    @Test
    @DisplayName("existsByCode should return 200 with false when code does not exist")
    void existsByCode_shouldReturn200WithFalseWhenNotExists() throws Exception {
        when(accountService.existsByCode("FAKE")).thenReturn(false);

        mockMvc.perform(get("/api/accounts/by-code/FAKE/exists").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));

        verify(accountService).existsByCode("FAKE");
    }

    @Test
    @DisplayName("create should return 201 with location header and created account")
    void create_shouldReturn201WithLocationAndBody() throws Exception {
        var created = Account.builder()
                .id(1L)
                .code("CASH")
                .description("Cash account")
                .balance(new BigDecimal("1000.00"))
                .isActive(true)
                .build();

        when(accountService.create(any())).thenReturn(created);

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"CASH\",\"description\":\"Cash account\",\"balance\":1000.00}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/accounts/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("CASH"));

        verify(accountService).create(any());
    }

    @Test
    @DisplayName("create should return 409 when code already exists")
    void create_shouldReturn409WhenCodeExists() throws Exception {
        when(accountService.create(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Account code already exists"));

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"CASH\",\"description\":\"Cash account\",\"balance\":1000.00}"))
                .andExpect(status().isConflict());

        verify(accountService).create(any());
    }

    @Test
    @DisplayName("update should return 200 with updated account")
    void update_shouldReturn200WithUpdatedAccount() throws Exception {
        var updated = Account.builder()
                .id(1L)
                .code("CASH")
                .description("Updated description")
                .balance(new BigDecimal("2000.00"))
                .isActive(true)
                .build();

        when(accountService.update(anyLong(), any())).thenReturn(updated);

        mockMvc.perform(put("/api/accounts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"CASH\",\"description\":\"Updated description\",\"balance\":2000.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Updated description"));

        verify(accountService).update(anyLong(), any());
    }

    @Test
    @DisplayName("update should return 404 when account not found")
    void update_shouldReturn404WhenNotFound() throws Exception {
        when(accountService.update(anyLong(), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        mockMvc.perform(put("/api/accounts/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"CASH\",\"description\":\"Cash account\",\"balance\":1000.00}"))
                .andExpect(status().isNotFound());

        verify(accountService).update(anyLong(), any());
    }

    @Test
    @DisplayName("update should return 409 when code conflicts with another account")
    void update_shouldReturn409WhenCodeConflicts() throws Exception {
        when(accountService.update(anyLong(), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Account code already exists"));

        mockMvc.perform(put("/api/accounts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"EXISTING\",\"description\":\"Cash account\",\"balance\":1000.00}"))
                .andExpect(status().isConflict());

        verify(accountService).update(anyLong(), any());
    }

    @Test
    @DisplayName("delete should return 204 when successful")
    void delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/accounts/1"))
                .andExpect(status().isNoContent());

        verify(accountService).delete(1L);
    }

    @Test
    @DisplayName("delete should return 404 when account not found")
    void delete_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"))
                .when(accountService).delete(anyLong());

        mockMvc.perform(delete("/api/accounts/99"))
                .andExpect(status().isNotFound());

        verify(accountService).delete(99L);
    }
}
