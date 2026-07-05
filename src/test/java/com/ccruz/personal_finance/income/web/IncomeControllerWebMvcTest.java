package com.ccruz.personal_finance.income.web;

import com.ccruz.personal_finance.account.persistence.Account;
import com.ccruz.personal_finance.account.web.dto.AccountResponse;
import com.ccruz.personal_finance.income.persistence.Income;
import com.ccruz.personal_finance.income.service.IncomeService;
import com.ccruz.personal_finance.income.web.dto.IncomeResponse;
import com.ccruz.personal_finance.income_category.persistence.IncomeCategory;
import com.ccruz.personal_finance.income_category.web.dto.IncomeCategoryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IncomeController.class)
@ActiveProfiles("test")
@DisplayName("IncomeController - @WebMvcTest")
class IncomeControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IncomeService incomeService;

    @Autowired
    private IncomeMapper incomeMapper;

    private IncomeCategory salaryCategory;
    private Account cashAccount;
    private IncomeCategoryResponse salaryCategoryResponse;
    private AccountResponse cashAccountResponse;

    @BeforeEach
    void setUp() {
        Mockito.reset(incomeService, incomeMapper);

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

        salaryCategoryResponse = new IncomeCategoryResponse(10L, "Salary", null);
        cashAccountResponse = new AccountResponse(20L, "CASH", "Cash account", new BigDecimal("1000.00"));
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
        var income1Response = new IncomeResponse(1L, salaryCategoryResponse, cashAccountResponse,
                new BigDecimal("3000.00"), LocalDate.of(2026, 6, 1), "Monthly salary");
        var income2Response = new IncomeResponse(2L, salaryCategoryResponse, cashAccountResponse,
                new BigDecimal("500.00"), LocalDate.of(2026, 6, 15), "Bonus");

        when(incomeService.findAll()).thenReturn(List.of(income1, income2));
        when(incomeMapper.toResponse(anyList())).thenReturn(List.of(income1Response, income2Response));

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
        when(incomeMapper.toResponse(anyList())).thenReturn(List.of());

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
        var activeResponse = new IncomeResponse(1L, salaryCategoryResponse, cashAccountResponse,
                new BigDecimal("3000.00"), LocalDate.of(2026, 6, 1), "Active");
        var inactiveResponse = new IncomeResponse(2L, salaryCategoryResponse, cashAccountResponse,
                new BigDecimal("500.00"), LocalDate.of(2026, 6, 15), "Inactive");

        when(incomeService.findAllIncludingInactive()).thenReturn(List.of(active, inactive));
        when(incomeMapper.toResponse(anyList())).thenReturn(List.of(activeResponse, inactiveResponse));

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
        when(incomeMapper.toResponse(anyList())).thenReturn(List.of());

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
        var response = new IncomeResponse(1L, salaryCategoryResponse, cashAccountResponse,
                new BigDecimal("3000.00"), LocalDate.of(2026, 6, 1), "Monthly salary");

        when(incomeService.findById(1L)).thenReturn(income);
        when(incomeMapper.toResponse(income)).thenReturn(response);

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
        var response = new IncomeResponse(1L, salaryCategoryResponse, cashAccountResponse,
                new BigDecimal("3000.00"), LocalDate.of(2026, 6, 1), "Monthly salary");

        when(incomeService.create(any())).thenReturn(created);
        when(incomeMapper.toResponse(created)).thenReturn(response);

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
    @DisplayName("create should return 400 when incomeCategoryId is null")
    void create_shouldReturn400WhenIncomeCategoryIdIsNull() throws Exception {
        mockMvc.perform(post("/api/incomes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountId\":20,\"amount\":3000.00,\"date\":\"2026-06-01\",\"description\":\"Monthly salary\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("create should return 400 when accountId is null")
    void create_shouldReturn400WhenAccountIdIsNull() throws Exception {
        mockMvc.perform(post("/api/incomes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"incomeCategoryId\":10,\"amount\":3000.00,\"date\":\"2026-06-01\",\"description\":\"Monthly salary\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("create should return 400 when amount is null")
    void create_shouldReturn400WhenAmountIsNull() throws Exception {
        mockMvc.perform(post("/api/incomes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"incomeCategoryId\":10,\"accountId\":20,\"date\":\"2026-06-01\",\"description\":\"Monthly salary\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("create should return 400 when date is null")
    void create_shouldReturn400WhenDateIsNull() throws Exception {
        mockMvc.perform(post("/api/incomes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"incomeCategoryId\":10,\"accountId\":20,\"amount\":3000.00,\"description\":\"Monthly salary\"}"))
                .andExpect(status().isBadRequest());
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
        var response = new IncomeResponse(1L, salaryCategoryResponse, cashAccountResponse,
                new BigDecimal("3500.00"), LocalDate.of(2026, 7, 1), "Updated salary");

        when(incomeService.update(anyLong(), any())).thenReturn(updated);
        when(incomeMapper.toResponse(updated)).thenReturn(response);

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
    @DisplayName("update should return 400 when incomeCategoryId is null")
    void update_shouldReturn400WhenIncomeCategoryIdIsNull() throws Exception {
        mockMvc.perform(put("/api/incomes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountId\":20,\"amount\":3500.00,\"date\":\"2026-07-01\",\"description\":\"Updated salary\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("update should return 400 when accountId is null")
    void update_shouldReturn400WhenAccountIdIsNull() throws Exception {
        mockMvc.perform(put("/api/incomes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"incomeCategoryId\":10,\"amount\":3500.00,\"date\":\"2026-07-01\",\"description\":\"Updated salary\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("update should return 400 when amount is null")
    void update_shouldReturn400WhenAmountIsNull() throws Exception {
        mockMvc.perform(put("/api/incomes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"incomeCategoryId\":10,\"accountId\":20,\"date\":\"2026-07-01\",\"description\":\"Updated salary\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("update should return 400 when date is null")
    void update_shouldReturn400WhenDateIsNull() throws Exception {
        mockMvc.perform(put("/api/incomes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"incomeCategoryId\":10,\"accountId\":20,\"amount\":3500.00,\"description\":\"Updated salary\"}"))
                .andExpect(status().isBadRequest());
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

    @TestConfiguration
    static class MockConfig {
        @Bean
        IncomeService incomeService() {
            return Mockito.mock(IncomeService.class);
        }

        @Bean
        IncomeMapper incomeMapper() {
            return Mockito.mock(IncomeMapper.class);
        }
    }
}
