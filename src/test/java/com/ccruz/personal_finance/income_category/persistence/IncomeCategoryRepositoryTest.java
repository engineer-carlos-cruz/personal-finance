package com.ccruz.personal_finance.income_category.persistence;

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
@DisplayName("IncomeCategoryRepository @DataJpaTest")
class IncomeCategoryRepositoryTest {

    @Autowired
    private IncomeCategoryRepository incomeCategoryRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    @DisplayName("findAll should return only active income categories when inactive categories exist")
    void findAll_shouldReturnOnlyActiveIncomeCategories() {
        var activeCategory = IncomeCategory.builder()
                .name("Salary")
                .description("Regular salary")
                .isActive(true)
                .build();
        var inactiveCategory = IncomeCategory.builder()
                .name("Deprecated")
                .description("Deprecated category")
                .isActive(false)
                .build();

        testEntityManager.persist(activeCategory);
        testEntityManager.persist(inactiveCategory);
        testEntityManager.flush();
        testEntityManager.clear();

        var result = incomeCategoryRepository.findAll();

        assertThat(result)
                .hasSize(1)
                .extracting(IncomeCategory::getName)
                .containsExactly("Salary");
    }
}
