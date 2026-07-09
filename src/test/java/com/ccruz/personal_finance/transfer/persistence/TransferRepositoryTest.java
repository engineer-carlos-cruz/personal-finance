package com.ccruz.personal_finance.transfer.persistence;

import com.ccruz.personal_finance.account.persistence.Account;
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
@DisplayName("TransferRepository @DataJpaTest")
class TransferRepositoryTest {

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    @DisplayName("should persist and find a transfer by id")
    void shouldPersistAndFindTransferById() {
        var source = Account.builder()
                .code("SOURCE")
                .description("Source account")
                .balance(new BigDecimal("1000.00"))
                .isActive(true)
                .build();
        var target = Account.builder()
                .code("TARGET")
                .description("Target account")
                .balance(new BigDecimal("500.00"))
                .isActive(true)
                .build();

        testEntityManager.persist(source);
        testEntityManager.persist(target);
        testEntityManager.flush();
        testEntityManager.clear();

        var transfer = Transfer.builder()
                .sourceAccount(source)
                .targetAccount(target)
                .amount(new BigDecimal("100.00"))
                .date(LocalDate.of(2026, 7, 9))
                .build();

        testEntityManager.persist(transfer);
        testEntityManager.flush();
        testEntityManager.clear();

        var result = transferRepository.findById(transfer.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(transfer.getId());
        assertThat(result.get().getSourceAccount().getCode()).isEqualTo("SOURCE");
        assertThat(result.get().getTargetAccount().getCode()).isEqualTo("TARGET");
        assertThat(result.get().getAmount()).isEqualByComparingTo("100.00");
        assertThat(result.get().getDate()).isEqualTo(LocalDate.of(2026, 7, 9));
    }

    @Test
    @DisplayName("findAll should return all persisted transfers")
    void findAll_shouldReturnAllPersistedTransfers() {
        var source1 = Account.builder()
                .code("CASH")
                .description("Cash")
                .balance(new BigDecimal("1000.00"))
                .isActive(true)
                .build();
        var target1 = Account.builder()
                .code("BANK")
                .description("Bank")
                .balance(new BigDecimal("2000.00"))
                .isActive(true)
                .build();
        var source2 = Account.builder()
                .code("SAVINGS")
                .description("Savings")
                .balance(new BigDecimal("3000.00"))
                .isActive(true)
                .build();
        var target2 = Account.builder()
                .code("INVEST")
                .description("Investment")
                .balance(new BigDecimal("4000.00"))
                .isActive(true)
                .build();

        testEntityManager.persist(source1);
        testEntityManager.persist(target1);
        testEntityManager.persist(source2);
        testEntityManager.persist(target2);
        testEntityManager.flush();
        testEntityManager.clear();

        var transfer1 = Transfer.builder()
                .sourceAccount(source1)
                .targetAccount(target1)
                .amount(new BigDecimal("100.00"))
                .date(LocalDate.of(2026, 7, 9))
                .build();
        var transfer2 = Transfer.builder()
                .sourceAccount(source2)
                .targetAccount(target2)
                .amount(new BigDecimal("200.00"))
                .date(LocalDate.of(2026, 7, 10))
                .build();

        testEntityManager.persist(transfer1);
        testEntityManager.persist(transfer2);
        testEntityManager.flush();
        testEntityManager.clear();

        var result = transferRepository.findAll();

        assertThat(result).hasSize(2);
    }
}
