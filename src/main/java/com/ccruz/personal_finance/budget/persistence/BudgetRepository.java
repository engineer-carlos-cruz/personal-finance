package com.ccruz.personal_finance.budget.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    @Query(value = "SELECT * FROM budgets", nativeQuery = true)
    List<Budget> findAllIncludingInactive();
}
