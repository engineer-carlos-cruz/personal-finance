package com.ccruz.personal_finance.expense.web;

import com.ccruz.personal_finance.account.web.dto.AccountResponse;
import com.ccruz.personal_finance.expense.persistence.Expense;
import com.ccruz.personal_finance.expense.service.ExpenseService;
import com.ccruz.personal_finance.expense.web.dto.ExpenseResponse;
import com.ccruz.personal_finance.expense_category.web.dto.ExpenseCategoryResponse;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExpenseController.class)
@ActiveProfiles("test")
@DisplayName("ExpenseController - @WebMvcTest")
class ExpenseControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private ExpenseMapper expenseMapper;

    @BeforeEach
    void setUp() {
        Mockito.reset(expenseService, expenseMapper);
    }

    // ========== findAll ==========

    @Test
    @DisplayName("findAll should return 200 with list of expenses")
    void findAll_shouldReturn200WithExpenses() throws Exception {
        var catResponse = new ExpenseCategoryResponse(1L, "Food", "Food expenses");
        var accResponse = new AccountResponse(1L, "CASH", "Cash account", new BigDecimal("1000.00"));
        var expense1 = new ExpenseResponse(1L, catResponse, accResponse, new BigDecimal("50.00"), LocalDate.of(2026, 7, 1), "Lunch");
        var expense2 = new ExpenseResponse(2L, catResponse, accResponse, new BigDecimal("30.00"), LocalDate.of(2026, 7, 2), "Coffee");

        when(expenseService.findAll()).thenReturn(List.of());
        when(expenseMapper.toResponse(anyList())).thenReturn(List.of(expense1, expense2));

        mockMvc.perform(get("/api/expenses").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].expenseCategory.id").value(1))
                .andExpect(jsonPath("$[0].expenseCategory.name").value("Food"))
                .andExpect(jsonPath("$[0].account.id").value(1))
                .andExpect(jsonPath("$[0].account.code").value("CASH"))
                .andExpect(jsonPath("$[0].amount").value(50.00))
                .andExpect(jsonPath("$[0].description").value("Lunch"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].description").value("Coffee"));

        verify(expenseService).findAll();
    }

    @Test
    @DisplayName("findAll should return 200 with empty list when no expenses exist")
    void findAll_shouldReturn200WithEmptyList() throws Exception {
        when(expenseService.findAll()).thenReturn(List.of());
        when(expenseMapper.toResponse(anyList())).thenReturn(List.of());

        mockMvc.perform(get("/api/expenses").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));

        verify(expenseService).findAll();
    }

    // ========== findAllIncludingInactive ==========

    @Test
    @DisplayName("findAllIncludingInactive should return 200 with list of expenses")
    void findAllIncludingInactive_shouldReturn200WithExpenses() throws Exception {
        var catResponse = new ExpenseCategoryResponse(1L, "Food", "Food expenses");
        var accResponse = new AccountResponse(1L, "CASH", "Cash account", new BigDecimal("1000.00"));
        var expense1 = new ExpenseResponse(1L, catResponse, accResponse, new BigDecimal("50.00"), LocalDate.of(2026, 7, 1), "Lunch");
        var expense2 = new ExpenseResponse(2L, catResponse, accResponse, new BigDecimal("100.00"), LocalDate.of(2026, 7, 2), "Old expense");

        when(expenseService.findAllIncludingInactive()).thenReturn(List.of());
        when(expenseMapper.toResponse(anyList())).thenReturn(List.of(expense1, expense2));

        mockMvc.perform(get("/api/expenses/with-inactive").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(expenseService).findAllIncludingInactive();
    }

    @Test
    @DisplayName("findAllIncludingInactive should return 200 with empty list")
    void findAllIncludingInactive_shouldReturn200WithEmptyList() throws Exception {
        when(expenseService.findAllIncludingInactive()).thenReturn(List.of());
        when(expenseMapper.toResponse(anyList())).thenReturn(List.of());

        mockMvc.perform(get("/api/expenses/with-inactive").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));

        verify(expenseService).findAllIncludingInactive();
    }

    // ========== findById ==========

    @Test
    @DisplayName("findById should return 200 with expense when found")
    void findById_shouldReturn200WhenFound() throws Exception {
        var catResponse = new ExpenseCategoryResponse(1L, "Food", "Food expenses");
        var accResponse = new AccountResponse(1L, "CASH", "Cash account", new BigDecimal("1000.00"));
        var response = new ExpenseResponse(1L, catResponse, accResponse, new BigDecimal("50.00"), LocalDate.of(2026, 7, 1), "Lunch");

        when(expenseService.findById(1L)).thenReturn(Expense.builder().build());
        when(expenseMapper.toResponse(any(Expense.class))).thenReturn(response);

        mockMvc.perform(get("/api/expenses/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.expenseCategory.name").value("Food"))
                .andExpect(jsonPath("$.account.code").value("CASH"))
                .andExpect(jsonPath("$.amount").value(50.00))
                .andExpect(jsonPath("$.description").value("Lunch"));

        verify(expenseService).findById(1L);
    }

    @Test
    @DisplayName("findById should return 404 when not found")
    void findById_shouldReturn404WhenNotFound() throws Exception {
        when(expenseService.findById(anyLong()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found"));

        mockMvc.perform(get("/api/expenses/99").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(expenseService).findById(99L);
    }

    // ========== create ==========

    @Test
    @DisplayName("create should return 201 with location header and created expense")
    void create_shouldReturn201WithLocationAndBody() throws Exception {
        var catResponse = new ExpenseCategoryResponse(1L, "Food", "Food expenses");
        var accResponse = new AccountResponse(1L, "CASH", "Cash account", new BigDecimal("1000.00"));
        var response = new ExpenseResponse(1L, catResponse, accResponse, new BigDecimal("50.00"), LocalDate.of(2026, 7, 1), "Lunch");

        when(expenseService.create(any())).thenReturn(Expense.builder().id(1L).build());
        when(expenseMapper.toResponse(any(Expense.class))).thenReturn(response);

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expenseCategoryId\":1,\"accountId\":1,\"amount\":50.00,\"date\":\"2026-07-01\",\"description\":\"Lunch\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/expenses/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.expenseCategory.id").value(1))
                .andExpect(jsonPath("$.account.id").value(1))
                .andExpect(jsonPath("$.amount").value(50.00));

        verify(expenseService).create(any());
    }

    @Test
    @DisplayName("create should return 400 when expenseCategoryId is null")
    void create_shouldReturn400WhenExpenseCategoryIdIsNull() throws Exception {
        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountId\":1,\"amount\":50.00,\"date\":\"2026-07-01\",\"description\":\"Lunch\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("create should return 400 when accountId is null")
    void create_shouldReturn400WhenAccountIdIsNull() throws Exception {
        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expenseCategoryId\":1,\"amount\":50.00,\"date\":\"2026-07-01\",\"description\":\"Lunch\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("create should return 400 when amount is null")
    void create_shouldReturn400WhenAmountIsNull() throws Exception {
        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expenseCategoryId\":1,\"accountId\":1,\"date\":\"2026-07-01\",\"description\":\"Lunch\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("create should return 400 when date is null")
    void create_shouldReturn400WhenDateIsNull() throws Exception {
        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expenseCategoryId\":1,\"accountId\":1,\"amount\":50.00,\"description\":\"Lunch\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("create should return 404 when expense category not found")
    void create_shouldReturn404WhenExpenseCategoryNotFound() throws Exception {
        when(expenseService.create(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense category not found"));

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expenseCategoryId\":99,\"accountId\":1,\"amount\":50.00,\"date\":\"2026-07-01\",\"description\":\"Lunch\"}"))
                .andExpect(status().isNotFound());

        verify(expenseService).create(any());
    }

    @Test
    @DisplayName("create should return 404 when account not found")
    void create_shouldReturn404WhenAccountNotFound() throws Exception {
        when(expenseService.create(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expenseCategoryId\":1,\"accountId\":99,\"amount\":50.00,\"date\":\"2026-07-01\",\"description\":\"Lunch\"}"))
                .andExpect(status().isNotFound());

        verify(expenseService).create(any());
    }

    // ========== update ==========

    @Test
    @DisplayName("update should return 200 with updated expense")
    void update_shouldReturn200WithUpdatedExpense() throws Exception {
        var catResponse = new ExpenseCategoryResponse(1L, "Food", "Food expenses");
        var accResponse = new AccountResponse(1L, "CASH", "Cash account", new BigDecimal("1000.00"));
        var response = new ExpenseResponse(1L, catResponse, accResponse, new BigDecimal("60.00"), LocalDate.of(2026, 7, 5), "Updated lunch");

        when(expenseService.update(anyLong(), any())).thenReturn(Expense.builder().build());
        when(expenseMapper.toResponse(any(Expense.class))).thenReturn(response);

        mockMvc.perform(put("/api/expenses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expenseCategoryId\":1,\"accountId\":1,\"amount\":60.00,\"date\":\"2026-07-05\",\"description\":\"Updated lunch\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(60.00))
                .andExpect(jsonPath("$.description").value("Updated lunch"));

        verify(expenseService).update(anyLong(), any());
    }

    @Test
    @DisplayName("update should return 400 when expenseCategoryId is null")
    void update_shouldReturn400WhenExpenseCategoryIdIsNull() throws Exception {
        mockMvc.perform(put("/api/expenses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountId\":1,\"amount\":50.00,\"date\":\"2026-07-01\",\"description\":\"Lunch\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("update should return 400 when accountId is null")
    void update_shouldReturn400WhenAccountIdIsNull() throws Exception {
        mockMvc.perform(put("/api/expenses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expenseCategoryId\":1,\"amount\":50.00,\"date\":\"2026-07-01\",\"description\":\"Lunch\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("update should return 400 when amount is null")
    void update_shouldReturn400WhenAmountIsNull() throws Exception {
        mockMvc.perform(put("/api/expenses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expenseCategoryId\":1,\"accountId\":1,\"date\":\"2026-07-01\",\"description\":\"Lunch\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("update should return 400 when date is null")
    void update_shouldReturn400WhenDateIsNull() throws Exception {
        mockMvc.perform(put("/api/expenses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expenseCategoryId\":1,\"accountId\":1,\"amount\":50.00,\"description\":\"Lunch\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("update should return 404 when expense not found")
    void update_shouldReturn404WhenExpenseNotFound() throws Exception {
        when(expenseService.update(anyLong(), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found"));

        mockMvc.perform(put("/api/expenses/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expenseCategoryId\":1,\"accountId\":1,\"amount\":50.00,\"date\":\"2026-07-01\",\"description\":\"Lunch\"}"))
                .andExpect(status().isNotFound());

        verify(expenseService).update(anyLong(), any());
    }

    @Test
    @DisplayName("update should return 404 when expense category not found")
    void update_shouldReturn404WhenExpenseCategoryNotFound() throws Exception {
        when(expenseService.update(anyLong(), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense category not found"));

        mockMvc.perform(put("/api/expenses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expenseCategoryId\":99,\"accountId\":1,\"amount\":50.00,\"date\":\"2026-07-01\",\"description\":\"Lunch\"}"))
                .andExpect(status().isNotFound());

        verify(expenseService).update(anyLong(), any());
    }

    @Test
    @DisplayName("update should return 404 when account not found")
    void update_shouldReturn404WhenAccountNotFound() throws Exception {
        when(expenseService.update(anyLong(), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        mockMvc.perform(put("/api/expenses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expenseCategoryId\":1,\"accountId\":99,\"amount\":50.00,\"date\":\"2026-07-01\",\"description\":\"Lunch\"}"))
                .andExpect(status().isNotFound());

        verify(expenseService).update(anyLong(), any());
    }

    // ========== delete ==========

    @Test
    @DisplayName("delete should return 204 when successful")
    void delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/expenses/1"))
                .andExpect(status().isNoContent());

        verify(expenseService).delete(1L);
    }

    @Test
    @DisplayName("delete should return 404 when expense not found")
    void delete_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found"))
                .when(expenseService).delete(anyLong());

        mockMvc.perform(delete("/api/expenses/99"))
                .andExpect(status().isNotFound());

        verify(expenseService).delete(99L);
    }

    @TestConfiguration
    static class MockConfig {
        @Bean
        ExpenseService expenseService() {
            return Mockito.mock(ExpenseService.class);
        }

        @Bean
        ExpenseMapper expenseMapper() {
            return Mockito.mock(ExpenseMapper.class);
        }
    }
}
