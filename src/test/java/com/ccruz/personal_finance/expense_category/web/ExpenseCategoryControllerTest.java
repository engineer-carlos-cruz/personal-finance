package com.ccruz.personal_finance.expense_category.web;

import com.ccruz.personal_finance.expense_category.persistence.ExpenseCategory;
import com.ccruz.personal_finance.expense_category.service.ExpenseCategoryService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExpenseCategoryController - Standalone")
class ExpenseCategoryControllerTest {

    @Mock
    private ExpenseCategoryService expenseCategoryService;

    @InjectMocks
    private ExpenseCategoryController expenseCategoryController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(expenseCategoryController).build();
    }

    @Test
    @DisplayName("findAll should return 200 with list of categories")
    void findAll_shouldReturn200WithCategories() throws Exception {
        var food = ExpenseCategory.builder()
                .id(1L)
                .name("Food")
                .description("Food expenses")
                .isActive(true)
                .build();
        var transport = ExpenseCategory.builder()
                .id(2L)
                .name("Transport")
                .description("Transport expenses")
                .isActive(true)
                .build();

        when(expenseCategoryService.findAll()).thenReturn(List.of(food, transport));

        mockMvc.perform(get("/api/expense-categories").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Food"))
                .andExpect(jsonPath("$[0].description").value("Food expenses"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Transport"));

        verify(expenseCategoryService).findAll();
    }

    @Test
    @DisplayName("findAll should return 200 with empty list when no categories exist")
    void findAll_shouldReturn200WithEmptyList() throws Exception {
        when(expenseCategoryService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/expense-categories").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));

        verify(expenseCategoryService).findAll();
    }

    @Test
    @DisplayName("findAllIncludingInactive should return 200 with list of categories")
    void findAllIncludingInactive_shouldReturn200WithCategories() throws Exception {
        var food = ExpenseCategory.builder()
                .id(1L)
                .name("Food")
                .description("Food expenses")
                .isActive(true)
                .build();
        var inactive = ExpenseCategory.builder()
                .id(2L)
                .name("Old Category")
                .description("Inactive category")
                .isActive(false)
                .build();

        when(expenseCategoryService.findAllIncludingInactive()).thenReturn(List.of(food, inactive));

        mockMvc.perform(get("/api/expense-categories/with-inactive").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(expenseCategoryService).findAllIncludingInactive();
    }

    @Test
    @DisplayName("findAllIncludingInactive should return 200 with empty list")
    void findAllIncludingInactive_shouldReturn200WithEmptyList() throws Exception {
        when(expenseCategoryService.findAllIncludingInactive()).thenReturn(List.of());

        mockMvc.perform(get("/api/expense-categories/with-inactive").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));

        verify(expenseCategoryService).findAllIncludingInactive();
    }

    @Test
    @DisplayName("findById should return 200 with category when found")
    void findById_shouldReturn200WhenFound() throws Exception {
        var category = ExpenseCategory.builder()
                .id(1L)
                .name("Food")
                .description("Food expenses")
                .isActive(true)
                .build();

        when(expenseCategoryService.findById(1L)).thenReturn(category);

        mockMvc.perform(get("/api/expense-categories/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Food"));

        verify(expenseCategoryService).findById(1L);
    }

    @Test
    @DisplayName("findById should return 404 when not found")
    void findById_shouldReturn404WhenNotFound() throws Exception {
        when(expenseCategoryService.findById(anyLong()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense category not found"));

        mockMvc.perform(get("/api/expense-categories/99").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(expenseCategoryService).findById(99L);
    }

    @Test
    @DisplayName("create should return 201 with created category")
    void create_shouldReturn201WithCreatedCategory() throws Exception {
        var created = ExpenseCategory.builder()
                .id(1L)
                .name("Food")
                .description("Food expenses")
                .isActive(true)
                .build();

        when(expenseCategoryService.create(any())).thenReturn(created);

        mockMvc.perform(post("/api/expense-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Food\",\"description\":\"Food expenses\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Food"));

        verify(expenseCategoryService).create(any());
    }

    @Test
    @DisplayName("update should return 200 with updated category")
    void update_shouldReturn200WithUpdatedCategory() throws Exception {
        var updated = ExpenseCategory.builder()
                .id(1L)
                .name("Food")
                .description("Updated description")
                .isActive(true)
                .build();

        when(expenseCategoryService.update(anyLong(), any())).thenReturn(updated);

        mockMvc.perform(put("/api/expense-categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Food\",\"description\":\"Updated description\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Updated description"));

        verify(expenseCategoryService).update(anyLong(), any());
    }

    @Test
    @DisplayName("update should return 404 when category not found")
    void update_shouldReturn404WhenNotFound() throws Exception {
        when(expenseCategoryService.update(anyLong(), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense category not found"));

        mockMvc.perform(put("/api/expense-categories/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Food\",\"description\":\"Food expenses\"}"))
                .andExpect(status().isNotFound());

        verify(expenseCategoryService).update(anyLong(), any());
    }

    @Test
    @DisplayName("delete should return 204 when successful")
    void delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/expense-categories/1"))
                .andExpect(status().isNoContent());

        verify(expenseCategoryService).delete(1L);
    }

    @Test
    @DisplayName("delete should return 404 when category not found")
    void delete_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense category not found"))
                .when(expenseCategoryService).delete(anyLong());

        mockMvc.perform(delete("/api/expense-categories/99"))
                .andExpect(status().isNotFound());

        verify(expenseCategoryService).delete(99L);
    }
}
