package com.ccruz.personal_finance.expense_category.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(excludeAutoConfiguration = FlywayAutoConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("ExpenseCategoryRepository @DataJpaTest")
class ExpenseCategoryRepositoryTest {

    @Autowired
    private ExpenseCategoryRepository expenseCategoryRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    @DisplayName("findAll should return only active categories when inactive categories exist")
    void findAll_shouldReturnOnlyActiveCategories() {
        var activeCategory = ExpenseCategory.builder()
                .name("Food")
                .description("Food expenses")
                .isActive(true)
                .build();
        var inactiveCategory = ExpenseCategory.builder()
                .name("Old Category")
                .description("Old expenses")
                .isActive(false)
                .build();

        testEntityManager.persist(activeCategory);
        testEntityManager.persist(inactiveCategory);
        testEntityManager.flush();
        testEntityManager.clear();

        var result = expenseCategoryRepository.findAll();

        assertThat(result)
                .hasSize(1)
                .extracting(ExpenseCategory::getName)
                .containsExactly("Food");
    }

    @Test
    @DisplayName("findAllIncludingInactive should return all categories including inactive ones")
    void findAllIncludingInactive_shouldReturnAllCategories() {
        var activeCategory = ExpenseCategory.builder()
                .name("Food")
                .description("Food expenses")
                .isActive(true)
                .build();
        var inactiveCategory = ExpenseCategory.builder()
                .name("Old Category")
                .description("Old expenses")
                .isActive(false)
                .build();

        testEntityManager.persist(activeCategory);
        testEntityManager.persist(inactiveCategory);
        testEntityManager.flush();
        testEntityManager.clear();

        var result = expenseCategoryRepository.findAllIncludingInactive();

        assertThat(result)
                .hasSize(2)
                .extracting(ExpenseCategory::getName)
                .containsExactlyInAnyOrder("Food", "Old Category");
    }
}
