package com.ccruz.personal_finance.expense.persistence;

import com.ccruz.personal_finance.account.persistence.Account;
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
@DisplayName("ExpenseRepository @DataJpaTest")
class ExpenseRepositoryTest {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    @DisplayName("findAll should return only active expenses when inactive expenses exist")
    void findAll_shouldReturnOnlyActiveExpenses() {
        var category = testEntityManager.persist(ExpenseCategory.builder()
                .name("Food")
                .description("Food expenses")
                .isActive(true)
                .build());
        var account = testEntityManager.persist(Account.builder()
                .code("CASH")
                .description("Cash account")
                .balance(new BigDecimal("1000.00"))
                .isActive(true)
                .build());

        var activeExpense = Expense.builder()
                .expenseCategory(category)
                .account(account)
                .amount(new BigDecimal("50.00"))
                .date(LocalDate.of(2026, 7, 1))
                .description("Lunch")
                .isActive(true)
                .build();
        var inactiveExpense = Expense.builder()
                .expenseCategory(category)
                .account(account)
                .amount(new BigDecimal("100.00"))
                .date(LocalDate.of(2026, 7, 2))
                .description("Old expense")
                .isActive(false)
                .build();

        testEntityManager.persist(activeExpense);
        testEntityManager.persist(inactiveExpense);
        testEntityManager.flush();
        testEntityManager.clear();

        var result = expenseRepository.findAll();

        assertThat(result)
                .hasSize(1)
                .extracting(expense -> expense.getDescription())
                .containsExactly("Lunch");
    }

    @Test
    @DisplayName("findAllIncludingInactive should return all expenses including inactive ones")
    void findAllIncludingInactive_shouldReturnAllExpenses() {
        var category = testEntityManager.persist(ExpenseCategory.builder()
                .name("Food")
                .description("Food expenses")
                .isActive(true)
                .build());
        var account = testEntityManager.persist(Account.builder()
                .code("CASH")
                .description("Cash account")
                .balance(new BigDecimal("1000.00"))
                .isActive(true)
                .build());

        var activeExpense = Expense.builder()
                .expenseCategory(category)
                .account(account)
                .amount(new BigDecimal("50.00"))
                .date(LocalDate.of(2026, 7, 1))
                .description("Lunch")
                .isActive(true)
                .build();
        var inactiveExpense = Expense.builder()
                .expenseCategory(category)
                .account(account)
                .amount(new BigDecimal("100.00"))
                .date(LocalDate.of(2026, 7, 2))
                .description("Old expense")
                .isActive(false)
                .build();

        testEntityManager.persist(activeExpense);
        testEntityManager.persist(inactiveExpense);
        testEntityManager.flush();
        testEntityManager.clear();

        var result = expenseRepository.findAllIncludingInactive();

        assertThat(result)
                .hasSize(2)
                .extracting(expense -> expense.getDescription())
                .containsExactlyInAnyOrder("Lunch", "Old expense");
    }
}
