package com.ccruz.personal_finance.income_category.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IncomeCategoryRepository extends JpaRepository<IncomeCategory, Long> {

    @Query(value = "SELECT * FROM income_categories", nativeQuery = true)
    List<IncomeCategory> findAllIncludingInactive();
}
