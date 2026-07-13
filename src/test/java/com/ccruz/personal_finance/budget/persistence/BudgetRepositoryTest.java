package com.ccruz.personal_finance.budget.persistence;

import com.ccruz.personal_finance.expense_category.persistence.ExpenseCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(excludeAutoConfiguration = FlywayAutoConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("BudgetRepository @DataJpaTest")
class BudgetRepositoryTest {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    @DisplayName("findAll should return only active budgets when inactive budgets exist")
    void findAll_shouldReturnOnlyActiveBudgets() {
        var category = ExpenseCategory.builder()
                .name("Food")
                .isActive(true)
                .build();
        testEntityManager.persist(category);
        testEntityManager.flush();

        var activeBudget = Budget.builder()
                .expenseCategory(category)
                .amount(new BigDecimal("500.00"))
                .initialDate(LocalDate.of(2025, 1, 1))
                .finalDate(LocalDate.of(2025, 12, 31))
                .state(BudgetState.NOT_STARTED)
                .isActive(true)
                .build();
        var inactiveBudget = Budget.builder()
                .expenseCategory(category)
                .amount(new BigDecimal("300.00"))
                .initialDate(LocalDate.of(2025, 1, 1))
                .finalDate(LocalDate.of(2025, 12, 31))
                .state(BudgetState.NOT_STARTED)
                .isActive(false)
                .build();

        testEntityManager.persist(activeBudget);
        testEntityManager.persist(inactiveBudget);
        testEntityManager.flush();
        testEntityManager.clear();

        var result = budgetRepository.findAll();

        assertThat(result)
                .hasSize(1)
                .extracting(budget -> budget.getAmount())
                .containsExactly(new BigDecimal("500.00"));
    }

    @Test
    @DisplayName("findAllIncludingInactive should return all budgets including inactive ones")
    void findAllIncludingInactive_shouldReturnAllBudgets() {
        var category = ExpenseCategory.builder()
                .name("Transport")
                .isActive(true)
                .build();
        testEntityManager.persist(category);
        testEntityManager.flush();

        var activeBudget = Budget.builder()
                .expenseCategory(category)
                .amount(new BigDecimal("200.00"))
                .initialDate(LocalDate.of(2025, 1, 1))
                .finalDate(LocalDate.of(2025, 12, 31))
                .state(BudgetState.NOT_STARTED)
                .isActive(true)
                .build();
        var inactiveBudget = Budget.builder()
                .expenseCategory(category)
                .amount(new BigDecimal("100.00"))
                .initialDate(LocalDate.of(2025, 1, 1))
                .finalDate(LocalDate.of(2025, 12, 31))
                .state(BudgetState.NOT_STARTED)
                .isActive(false)
                .build();

        testEntityManager.persist(activeBudget);
        testEntityManager.persist(inactiveBudget);
        testEntityManager.flush();
        testEntityManager.clear();

        var result = budgetRepository.findAllIncludingInactive();

        assertThat(result)
                .hasSize(2)
                .extracting(budget -> budget.getAmount())
                .containsExactlyInAnyOrder(new BigDecimal("200.00"), new BigDecimal("100.00"));
    }
}
