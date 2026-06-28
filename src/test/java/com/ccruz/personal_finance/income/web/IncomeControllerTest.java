package com.ccruz.personal_finance.income.web;

import com.ccruz.personal_finance.account.persistence.Account;
import com.ccruz.personal_finance.income.persistence.Income;
import com.ccruz.personal_finance.income.service.IncomeService;
import com.ccruz.personal_finance.income_category.persistence.IncomeCategory;
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
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("IncomeController - Standalone")
class IncomeControllerTest {

    @Mock
    private IncomeService incomeService;

    @InjectMocks
    private IncomeController incomeController;

    private MockMvc mockMvc;

    private IncomeCategory salaryCategory;
    private Account cashAccount;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(incomeController).build();

        salaryCategory = IncomeCategory.builder()
                .id(10L)
                .name("Salary")
                .build();

        cashAccount = Account.builder()
                .id(20L)
                .code("CASH")
                .description("Cash account")
                .balance(new BigDecimal("1000.00"))
                .build();
    }

    @Test
    @DisplayName("findAll should return 200 with list of incomes")
    void findAll_shouldReturn200WithIncomes() throws Exception {
        var income1 = Income.builder()
                .id(1L)
                .incomeCategory(salaryCategory)
                .account(cashAccount)
                .amount(new BigDecimal("3000.00"))
                .date(LocalDate.of(2026, 6, 1))
                .description("Monthly salary")
                .build();
        var income2 = Income.builder()
                .id(2L)
                .incomeCategory(salaryCategory)
                .account(cashAccount)
                .amount(new BigDecimal("500.00"))
                .date(LocalDate.of(2026, 6, 15))
                .description("Bonus")
                .build();

        when(incomeService.findAll()).thenReturn(List.of(income1, income2));

        mockMvc.perform(get("/api/incomes").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].incomeCategory.id").value(10))
                .andExpect(jsonPath("$[0].incomeCategory.name").value("Salary"))
                .andExpect(jsonPath("$[0].account.id").value(20))
                .andExpect(jsonPath("$[0].account.code").value("CASH"))
                .andExpect(jsonPath("$[0].amount").value(3000.00))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].amount").value(500.00));

        verify(incomeService).findAll();
    }

    @Test
    @DisplayName("findAll should return 200 with empty list when no incomes exist")
    void findAll_shouldReturn200WithEmptyList() throws Exception {
        when(incomeService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/incomes").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));

        verify(incomeService).findAll();
    }

    @Test
    @DisplayName("findAllIncludingInactive should return 200 with list of incomes")
    void findAllIncludingInactive_shouldReturn200WithIncomes() throws Exception {
        var active = Income.builder()
                .id(1L)
                .incomeCategory(salaryCategory)
                .account(cashAccount)
                .amount(new BigDecimal("3000.00"))
                .date(LocalDate.of(2026, 6, 1))
                .description("Active")
                .build();
        var inactive = Income.builder()
                .id(2L)
                .incomeCategory(salaryCategory)
                .account(cashAccount)
                .amount(new BigDecimal("500.00"))
                .date(LocalDate.of(2026, 6, 15))
                .description("Inactive")
                .isActive(false)
                .build();

        when(incomeService.findAllIncludingInactive()).thenReturn(List.of(active, inactive));

        mockMvc.perform(get("/api/incomes/with-inactive").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(incomeService).findAllIncludingInactive();
    }

    @Test
    @DisplayName("findAllIncludingInactive should return 200 with empty list")
    void findAllIncludingInactive_shouldReturn200WithEmptyList() throws Exception {
        when(incomeService.findAllIncludingInactive()).thenReturn(List.of());

        mockMvc.perform(get("/api/incomes/with-inactive").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));

        verify(incomeService).findAllIncludingInactive();
    }

    @Test
    @DisplayName("findById should return 200 with income when found")
    void findById_shouldReturn200WhenFound() throws Exception {
        var income = Income.builder()
                .id(1L)
                .incomeCategory(salaryCategory)
                .account(cashAccount)
                .amount(new BigDecimal("3000.00"))
                .date(LocalDate.of(2026, 6, 1))
                .description("Monthly salary")
                .build();

        when(incomeService.findById(1L)).thenReturn(income);

        mockMvc.perform(get("/api/incomes/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.incomeCategory.name").value("Salary"))
                .andExpect(jsonPath("$.account.code").value("CASH"))
                .andExpect(jsonPath("$.amount").value(3000.00))
                .andExpect(jsonPath("$.description").value("Monthly salary"));

        verify(incomeService).findById(1L);
    }

    @Test
    @DisplayName("findById should return 404 when not found")
    void findById_shouldReturn404WhenNotFound() throws Exception {
        when(incomeService.findById(anyLong()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Income not found"));

        mockMvc.perform(get("/api/incomes/99").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(incomeService).findById(99L);
    }

    @Test
    @DisplayName("create should return 201 with location header and created income")
    void create_shouldReturn201WithLocationAndBody() throws Exception {
        var created = Income.builder()
                .id(1L)
                .incomeCategory(salaryCategory)
                .account(cashAccount)
                .amount(new BigDecimal("3000.00"))
                .date(LocalDate.of(2026, 6, 1))
                .description("Monthly salary")
                .build();

        when(incomeService.create(any())).thenReturn(created);

        mockMvc.perform(post("/api/incomes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"incomeCategoryId\":10,\"accountId\":20,\"amount\":3000.00,\"date\":\"2026-06-01\",\"description\":\"Monthly salary\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/incomes/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(3000.00))
                .andExpect(jsonPath("$.description").value("Monthly salary"));

        verify(incomeService).create(any());
    }

    @Test
    @DisplayName("update should return 200 with updated income")
    void update_shouldReturn200WithUpdatedIncome() throws Exception {
        var updated = Income.builder()
                .id(1L)
                .incomeCategory(salaryCategory)
                .account(cashAccount)
                .amount(new BigDecimal("3500.00"))
                .date(LocalDate.of(2026, 7, 1))
                .description("Updated salary")
                .build();

        when(incomeService.update(anyLong(), any())).thenReturn(updated);

        mockMvc.perform(put("/api/incomes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"incomeCategoryId\":10,\"accountId\":20,\"amount\":3500.00,\"date\":\"2026-07-01\",\"description\":\"Updated salary\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(3500.00))
                .andExpect(jsonPath("$.description").value("Updated salary"));

        verify(incomeService).update(anyLong(), any());
    }

    @Test
    @DisplayName("update should return 404 when income not found")
    void update_shouldReturn404WhenNotFound() throws Exception {
        when(incomeService.update(anyLong(), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Income not found"));

        mockMvc.perform(put("/api/incomes/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"incomeCategoryId\":10,\"accountId\":20,\"amount\":3000.00,\"date\":\"2026-06-01\",\"description\":\"Monthly salary\"}"))
                .andExpect(status().isNotFound());

        verify(incomeService).update(anyLong(), any());
    }

    @Test
    @DisplayName("delete should return 204 when successful")
    void delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/incomes/1"))
                .andExpect(status().isNoContent());

        verify(incomeService).delete(1L);
    }

    @Test
    @DisplayName("delete should return 404 when income not found")
    void delete_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Income not found"))
                .when(incomeService).delete(anyLong());

        mockMvc.perform(delete("/api/incomes/99"))
                .andExpect(status().isNotFound());

        verify(incomeService).delete(99L);
    }
}
