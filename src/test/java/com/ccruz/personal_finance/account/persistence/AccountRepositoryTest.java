package com.ccruz.personal_finance.account.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(excludeAutoConfiguration = FlywayAutoConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("AccountRepository @DataJpaTest")
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    @DisplayName("findAll should return only active accounts when inactive accounts exist")
    void findAll_shouldReturnOnlyActiveAccounts() {
        var activeAccount = Account.builder()
                .code("CASH")
                .description("Cash account")
                .balance(new BigDecimal("1000.00"))
                .isActive(true)
                .build();
        var inactiveAccount = Account.builder()
                .code("CLOSED")
                .description("Closed account")
                .balance(BigDecimal.ZERO)
                .isActive(false)
                .build();

        testEntityManager.persist(activeAccount);
        testEntityManager.persist(inactiveAccount);
        testEntityManager.flush();
        testEntityManager.clear();

        var result = accountRepository.findAll();

        assertThat(result)
                .hasSize(1)
                .extracting(account -> account.getCode())
                .containsExactly("CASH");
    }
}
