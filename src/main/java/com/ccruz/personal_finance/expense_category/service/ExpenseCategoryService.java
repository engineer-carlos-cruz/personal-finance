package com.ccruz.personal_finance.expense_category.service;

import com.ccruz.personal_finance.expense_category.persistence.ExpenseCategory;
import com.ccruz.personal_finance.expense_category.persistence.ExpenseCategoryRepository;
import com.ccruz.personal_finance.expense_category.web.dto.ExpenseCategoryUpsertRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ExpenseCategoryService {

    private final ExpenseCategoryRepository expenseCategoryRepository;

    public ExpenseCategoryService(ExpenseCategoryRepository expenseCategoryRepository) {
        this.expenseCategoryRepository = expenseCategoryRepository;
    }

    @Transactional(readOnly = true)
    public List<ExpenseCategory> findAll() {
        return expenseCategoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<ExpenseCategory> findAllIncludingInactive() {
        return expenseCategoryRepository.findAllIncludingInactive();
    }

    @Transactional(readOnly = true)
    public ExpenseCategory findById(Long id) {
        return expenseCategoryRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense category not found"));
    }

    @Transactional
    public ExpenseCategory create(ExpenseCategoryUpsertRequest request) {
        ExpenseCategory expenseCategory = ExpenseCategory.builder()
            .name(request.name().trim())
            .description(request.description() == null ? null : request.description().trim())
            .build();

        return expenseCategoryRepository.save(expenseCategory);
    }

    @Transactional
    public ExpenseCategory update(Long id, ExpenseCategoryUpsertRequest request) {
        ExpenseCategory expenseCategory = findById(id);

        expenseCategory.setName(request.name().trim());
        expenseCategory.setDescription(request.description() == null ? null : request.description().trim());

        return expenseCategoryRepository.save(expenseCategory);
    }

    @Transactional
    public void delete(Long id) {
        ExpenseCategory expenseCategory = findById(id);
        expenseCategoryRepository.delete(expenseCategory);
    }
}
