package com.ccruz.personal_finance.income_category.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IncomeCategoryRepository extends JpaRepository<IncomeCategory, Long> {
}
