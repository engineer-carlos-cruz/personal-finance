package com.ccruz.personal_finance.income.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IncomeRepository extends JpaRepository<Income, Long> {

    @Query(value = "SELECT * FROM incomes", nativeQuery = true)
    List<Income> findAllIncludingInactive();
}
