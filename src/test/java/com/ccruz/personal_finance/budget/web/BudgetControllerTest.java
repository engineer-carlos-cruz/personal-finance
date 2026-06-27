package com.ccruz.personal_finance.budget.web;

import com.ccruz.personal_finance.budget.persistence.Budget;
import com.ccruz.personal_finance.budget.persistence.BudgetState;
import com.ccruz.personal_finance.budget.service.BudgetService;
import com.ccruz.personal_finance.expense_category.persistence.ExpenseCategory;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("BudgetController - Standalone")
class BudgetControllerTest {

    @Mock
    private BudgetService budgetService;

    @InjectMocks
    private BudgetController budgetController;

    private MockMvc mockMvc;

    private ExpenseCategory category;
    private Budget budget;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(budgetController).build();

        category = ExpenseCategory.builder()
                .id(1L)
                .name("Food")
                .description("Food expenses")
                .isActive(true)
                .build();

        budget = Budget.builder()
                .id(1L)
                .expenseCategory(category)
                .amount(new BigDecimal("500.00"))
                .initialDate(LocalDate.of(2026, 1, 1))
                .finalDate(LocalDate.of(2026, 1, 31))
                .state(BudgetState.NOT_STARTED)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("findAll should return 200 with list of budgets")
    void findAll_shouldReturn200WithBudgets() throws Exception {
        var budget2 = Budget.builder()
                .id(2L)
                .expenseCategory(category)
                .amount(new BigDecimal("1000.00"))
                .initialDate(LocalDate.of(2026, 2, 1))
                .finalDate(LocalDate.of(2026, 2, 28))
                .state(BudgetState.WITHIN)
                .isActive(true)
                .build();

        when(budgetService.findAll()).thenReturn(List.of(budget, budget2));

        mockMvc.perform(get("/api/budgets").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].expenseCategory.id").value(1))
                .andExpect(jsonPath("$[0].expenseCategory.name").value("Food"))
                .andExpect(jsonPath("$[0].amount").value(500.00))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(budgetService).findAll();
    }

    @Test
    @DisplayName("findAll should return 200 with empty list when no budgets exist")
    void findAll_shouldReturn200WithEmptyList() throws Exception {
        when(budgetService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/budgets").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));

        verify(budgetService).findAll();
    }

    @Test
    @DisplayName("findAllIncludingInactive should return 200 with list of budgets")
    void findAllIncludingInactive_shouldReturn200WithBudgets() throws Exception {
        var inactiveBudget = Budget.builder()
                .id(2L)
                .expenseCategory(category)
                .amount(new BigDecimal("1000.00"))
                .initialDate(LocalDate.of(2026, 2, 1))
                .finalDate(LocalDate.of(2026, 2, 28))
                .state(BudgetState.FULLY_USED)
                .isActive(false)
                .build();

        when(budgetService.findAllIncludingInactive()).thenReturn(List.of(budget, inactiveBudget));

        mockMvc.perform(get("/api/budgets/with-inactive").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(budgetService).findAllIncludingInactive();
    }

    @Test
    @DisplayName("findAllIncludingInactive should return 200 with empty list")
    void findAllIncludingInactive_shouldReturn200WithEmptyList() throws Exception {
        when(budgetService.findAllIncludingInactive()).thenReturn(List.of());

        mockMvc.perform(get("/api/budgets/with-inactive").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));

        verify(budgetService).findAllIncludingInactive();
    }

    @Test
    @DisplayName("findById should return 200 with budget when found")
    void findById_shouldReturn200WhenFound() throws Exception {
        when(budgetService.findById(1L)).thenReturn(budget);

        mockMvc.perform(get("/api/budgets/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.expenseCategory.name").value("Food"))
                .andExpect(jsonPath("$.amount").value(500.00));

        verify(budgetService).findById(1L);
    }

    @Test
    @DisplayName("findById should return 404 when not found")
    void findById_shouldReturn404WhenNotFound() throws Exception {
        when(budgetService.findById(anyLong()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Budget not found"));

        mockMvc.perform(get("/api/budgets/99").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(budgetService).findById(99L);
    }

    @Test
    @DisplayName("create should return 201 with created budget")
    void create_shouldReturn201() throws Exception {
        when(budgetService.create(any())).thenReturn(budget);

        mockMvc.perform(post("/api/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expenseCategoryId\":1,\"amount\":500.00,\"initialDate\":\"2026-01-01\",\"finalDate\":\"2026-01-31\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.expenseCategory.name").value("Food"));

        verify(budgetService).create(any());
    }

    @Test
    @DisplayName("update should return 200 with updated budget")
    void update_shouldReturn200WithUpdatedBudget() throws Exception {
        when(budgetService.update(anyLong(), any())).thenReturn(budget);

        mockMvc.perform(put("/api/budgets/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expenseCategoryId\":1,\"amount\":500.00,\"initialDate\":\"2026-01-01\",\"finalDate\":\"2026-01-31\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(500.00));

        verify(budgetService).update(anyLong(), any());
    }

    @Test
    @DisplayName("update should return 404 when budget not found")
    void update_shouldReturn404WhenBudgetNotFound() throws Exception {
        when(budgetService.update(anyLong(), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Budget not found"));

        mockMvc.perform(put("/api/budgets/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expenseCategoryId\":1,\"amount\":500.00,\"initialDate\":\"2026-01-01\",\"finalDate\":\"2026-01-31\"}"))
                .andExpect(status().isNotFound());

        verify(budgetService).update(anyLong(), any());
    }

    @Test
    @DisplayName("update should return 404 when expense category not found")
    void update_shouldReturn404WhenCategoryNotFound() throws Exception {
        when(budgetService.update(anyLong(), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense category not found"));

        mockMvc.perform(put("/api/budgets/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expenseCategoryId\":99,\"amount\":500.00,\"initialDate\":\"2026-01-01\",\"finalDate\":\"2026-01-31\"}"))
                .andExpect(status().isNotFound());

        verify(budgetService).update(anyLong(), any());
    }

    @Test
    @DisplayName("delete should return 204 when successful")
    void delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/budgets/1"))
                .andExpect(status().isNoContent());

        verify(budgetService).delete(1L);
    }

    @Test
    @DisplayName("delete should return 404 when budget not found")
    void delete_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Budget not found"))
                .when(budgetService).delete(anyLong());

        mockMvc.perform(delete("/api/budgets/99"))
                .andExpect(status().isNotFound());

        verify(budgetService).delete(99L);
    }
}
