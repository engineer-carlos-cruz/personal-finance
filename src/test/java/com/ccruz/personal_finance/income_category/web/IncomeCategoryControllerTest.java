package com.ccruz.personal_finance.income_category.web;

import com.ccruz.personal_finance.income_category.persistence.IncomeCategory;
import com.ccruz.personal_finance.income_category.service.IncomeCategoryService;
import com.ccruz.personal_finance.income_category.web.dto.IncomeCategoryResponse;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("IncomeCategoryController - Standalone")
class IncomeCategoryControllerTest {

    @Mock
    private IncomeCategoryService incomeCategoryService;

    @Mock
    private IncomeCategoryMapper incomeCategoryMapper;

    @InjectMocks
    private IncomeCategoryController incomeCategoryController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(incomeCategoryController).build();
    }

    @Test
    @DisplayName("findAll should return 200 with list of categories")
    void findAll_shouldReturn200WithCategories() throws Exception {
        var salary = IncomeCategory.builder()
                .id(1L)
                .name("Salary")
                .description("Monthly salary")
                .isActive(true)
                .build();
        var freelance = IncomeCategory.builder()
                .id(2L)
                .name("Freelance")
                .description("Freelance income")
                .isActive(true)
                .build();
        var salaryResponse = new IncomeCategoryResponse(1L, "Salary", "Monthly salary");
        var freelanceResponse = new IncomeCategoryResponse(2L, "Freelance", "Freelance income");

        when(incomeCategoryService.findAll()).thenReturn(List.of(salary, freelance));
        when(incomeCategoryMapper.toResponse(anyList())).thenReturn(List.of(salaryResponse, freelanceResponse));

        mockMvc.perform(get("/api/income-categories").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Salary"))
                .andExpect(jsonPath("$[0].description").value("Monthly salary"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Freelance"));

        verify(incomeCategoryService).findAll();
    }

    @Test
    @DisplayName("findAll should return 200 with empty list when no categories exist")
    void findAll_shouldReturn200WithEmptyList() throws Exception {
        when(incomeCategoryService.findAll()).thenReturn(List.of());
        when(incomeCategoryMapper.toResponse(anyList())).thenReturn(List.of());

        mockMvc.perform(get("/api/income-categories").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));

        verify(incomeCategoryService).findAll();
    }

    @Test
    @DisplayName("findAllIncludingInactive should return 200 with list of categories")
    void findAllIncludingInactive_shouldReturn200WithCategories() throws Exception {
        var salary = IncomeCategory.builder()
                .id(1L)
                .name("Salary")
                .description("Monthly salary")
                .isActive(true)
                .build();
        var inactive = IncomeCategory.builder()
                .id(2L)
                .name("Old Category")
                .description("Inactive category")
                .isActive(false)
                .build();
        var salaryResponse = new IncomeCategoryResponse(1L, "Salary", "Monthly salary");
        var inactiveResponse = new IncomeCategoryResponse(2L, "Old Category", "Inactive category");

        when(incomeCategoryService.findAllIncludingInactive()).thenReturn(List.of(salary, inactive));
        when(incomeCategoryMapper.toResponse(anyList())).thenReturn(List.of(salaryResponse, inactiveResponse));

        mockMvc.perform(get("/api/income-categories/with-inactive").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(incomeCategoryService).findAllIncludingInactive();
    }

    @Test
    @DisplayName("findAllIncludingInactive should return 200 with empty list")
    void findAllIncludingInactive_shouldReturn200WithEmptyList() throws Exception {
        when(incomeCategoryService.findAllIncludingInactive()).thenReturn(List.of());
        when(incomeCategoryMapper.toResponse(anyList())).thenReturn(List.of());

        mockMvc.perform(get("/api/income-categories/with-inactive").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));

        verify(incomeCategoryService).findAllIncludingInactive();
    }

    @Test
    @DisplayName("findById should return 200 with category when found")
    void findById_shouldReturn200WhenFound() throws Exception {
        var category = IncomeCategory.builder()
                .id(1L)
                .name("Salary")
                .description("Monthly salary")
                .isActive(true)
                .build();
        var response = new IncomeCategoryResponse(1L, "Salary", "Monthly salary");

        when(incomeCategoryService.findById(1L)).thenReturn(category);
        when(incomeCategoryMapper.toResponse(category)).thenReturn(response);

        mockMvc.perform(get("/api/income-categories/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Salary"));

        verify(incomeCategoryService).findById(1L);
    }

    @Test
    @DisplayName("findById should return 404 when not found")
    void findById_shouldReturn404WhenNotFound() throws Exception {
        when(incomeCategoryService.findById(anyLong()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Income category not found"));

        mockMvc.perform(get("/api/income-categories/99").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(incomeCategoryService).findById(99L);
    }

    @Test
    @DisplayName("create should return 201 with created category")
    void create_shouldReturn201WithCreatedCategory() throws Exception {
        var created = IncomeCategory.builder()
                .id(1L)
                .name("Salary")
                .description("Monthly salary")
                .isActive(true)
                .build();
        var response = new IncomeCategoryResponse(1L, "Salary", "Monthly salary");

        when(incomeCategoryService.create(any())).thenReturn(created);
        when(incomeCategoryMapper.toResponse(created)).thenReturn(response);

        mockMvc.perform(post("/api/income-categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Salary\",\"description\":\"Monthly salary\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Salary"));

        verify(incomeCategoryService).create(any());
    }

    @Test
    @DisplayName("update should return 200 with updated category")
    void update_shouldReturn200WithUpdatedCategory() throws Exception {
        var updated = IncomeCategory.builder()
                .id(1L)
                .name("Salary")
                .description("Updated description")
                .isActive(true)
                .build();
        var response = new IncomeCategoryResponse(1L, "Salary", "Updated description");

        when(incomeCategoryService.update(anyLong(), any())).thenReturn(updated);
        when(incomeCategoryMapper.toResponse(updated)).thenReturn(response);

        mockMvc.perform(put("/api/income-categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Salary\",\"description\":\"Updated description\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Updated description"));

        verify(incomeCategoryService).update(anyLong(), any());
    }

    @Test
    @DisplayName("update should return 404 when category not found")
    void update_shouldReturn404WhenNotFound() throws Exception {
        when(incomeCategoryService.update(anyLong(), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Income category not found"));

        mockMvc.perform(put("/api/income-categories/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Salary\",\"description\":\"Monthly salary\"}"))
                .andExpect(status().isNotFound());

        verify(incomeCategoryService).update(anyLong(), any());
    }

    @Test
    @DisplayName("delete should return 204 when successful")
    void delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/income-categories/1"))
                .andExpect(status().isNoContent());

        verify(incomeCategoryService).delete(1L);
    }

    @Test
    @DisplayName("delete should return 404 when category not found")
    void delete_shouldReturn404WhenNotFound() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Income category not found"))
                .when(incomeCategoryService).delete(anyLong());

        mockMvc.perform(delete("/api/income-categories/99"))
                .andExpect(status().isNotFound());

        verify(incomeCategoryService).delete(99L);
    }
}
