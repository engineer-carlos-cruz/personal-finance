package com.ccruz.personal_finance.expense.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query(value = "SELECT * FROM expenses", nativeQuery = true)
    List<Expense> findAllIncludingInactive();
}
