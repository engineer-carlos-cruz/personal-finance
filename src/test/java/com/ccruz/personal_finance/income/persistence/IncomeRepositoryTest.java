package com.ccruz.personal_finance.income.persistence;

import com.ccruz.personal_finance.account.persistence.Account;
import com.ccruz.personal_finance.income_category.persistence.IncomeCategory;
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
@DisplayName("IncomeRepository @DataJpaTest")
class IncomeRepositoryTest {

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    @DisplayName("findAll should return only active incomes when inactive incomes exist")
    void findAll_shouldReturnOnlyActiveIncomes() {
        var category = IncomeCategory.builder()
                .name("Salary")
                .isActive(true)
                .build();
        var account = Account.builder()
                .code("SAVINGS")
                .description("Savings account")
                .balance(BigDecimal.ZERO)
                .isActive(true)
                .build();
        testEntityManager.persist(category);
        testEntityManager.persist(account);
        testEntityManager.flush();

        var activeIncome = Income.builder()
                .incomeCategory(category)
                .account(account)
                .amount(new BigDecimal("3000.00"))
                .date(LocalDate.of(2025, 1, 15))
                .isActive(true)
                .build();
        var inactiveIncome = Income.builder()
                .incomeCategory(category)
                .account(account)
                .amount(new BigDecimal("1500.00"))
                .date(LocalDate.of(2025, 1, 15))
                .description("Old income")
                .isActive(false)
                .build();

        testEntityManager.persist(activeIncome);
        testEntityManager.persist(inactiveIncome);
        testEntityManager.flush();
        testEntityManager.clear();

        var result = incomeRepository.findAll();

        assertThat(result)
                .hasSize(1)
                .extracting(income -> income.getAmount())
                .containsExactly(new BigDecimal("3000.00"));
    }

    @Test
    @DisplayName("findAllIncludingInactive should return all incomes including inactive ones")
    void findAllIncludingInactive_shouldReturnAllIncomes() {
        var category = IncomeCategory.builder()
                .name("Freelance")
                .isActive(true)
                .build();
        var account = Account.builder()
                .code("CHECKING")
                .description("Checking account")
                .balance(BigDecimal.ZERO)
                .isActive(true)
                .build();
        testEntityManager.persist(category);
        testEntityManager.persist(account);
        testEntityManager.flush();

        var activeIncome = Income.builder()
                .incomeCategory(category)
                .account(account)
                .amount(new BigDecimal("1200.00"))
                .date(LocalDate.of(2025, 2, 1))
                .isActive(true)
                .build();
        var inactiveIncome = Income.builder()
                .incomeCategory(category)
                .account(account)
                .amount(new BigDecimal("800.00"))
                .date(LocalDate.of(2025, 2, 1))
                .description("Old freelance")
                .isActive(false)
                .build();

        testEntityManager.persist(activeIncome);
        testEntityManager.persist(inactiveIncome);
        testEntityManager.flush();
        testEntityManager.clear();

        var result = incomeRepository.findAllIncludingInactive();

        assertThat(result)
                .hasSize(2)
                .extracting(income -> income.getAmount())
                .containsExactlyInAnyOrder(new BigDecimal("1200.00"), new BigDecimal("800.00"));
    }
}
