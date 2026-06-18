package com.ccruz.personal_finance.income_category.service;

import com.ccruz.personal_finance.income_category.persistence.IncomeCategory;
import com.ccruz.personal_finance.income_category.persistence.IncomeCategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("IncomeCategoryService")
class IncomeCategoryServiceTest {

    @Mock
    private IncomeCategoryRepository incomeCategoryRepository;

    @InjectMocks
    private IncomeCategoryService incomeCategoryService;

    @Test
    @DisplayName("findAll should return all active income categories")
    void findAll_shouldReturnAllActiveIncomeCategories() {
        var category1 = IncomeCategory.builder()
                .id(1L)
                .name("Salary")
                .description("Regular salary")
                .isActive(true)
                .build();
        var category2 = IncomeCategory.builder()
                .id(2L)
                .name("Freelance")
                .description("Freelance work")
                .isActive(true)
                .build();

        when(incomeCategoryRepository.findAll()).thenReturn(List.of(category1, category2));

        var result = incomeCategoryService.findAll();

        assertThat(result)
                .hasSize(2)
                .containsExactly(category1, category2);
    }

    @Test
    @DisplayName("findAll should return empty list when no income categories exist")
    void findAll_shouldReturnEmptyList_whenNoIncomeCategoriesExist() {
        when(incomeCategoryRepository.findAll()).thenReturn(List.of());

        var result = incomeCategoryService.findAll();

        assertThat(result).isEmpty();
    }
}
