package com.ccruz.personal_finance.expense_category.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, Long> {

    @Query(value = "SELECT * FROM expense_categories", nativeQuery = true)
    List<ExpenseCategory> findAllIncludingInactive();
}
