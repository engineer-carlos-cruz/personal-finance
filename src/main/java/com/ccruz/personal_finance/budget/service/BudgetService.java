package com.ccruz.personal_finance.budget.service;

import com.ccruz.personal_finance.budget.persistence.Budget;
import com.ccruz.personal_finance.budget.persistence.BudgetRepository;
import com.ccruz.personal_finance.budget.persistence.BudgetState;
import com.ccruz.personal_finance.budget.web.dto.BudgetUpsertRequest;
import com.ccruz.personal_finance.expense_category.persistence.ExpenseCategory;
import com.ccruz.personal_finance.expense_category.persistence.ExpenseCategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final ExpenseCategoryRepository expenseCategoryRepository;

    public BudgetService(BudgetRepository budgetRepository, ExpenseCategoryRepository expenseCategoryRepository) {
        this.budgetRepository = budgetRepository;
        this.expenseCategoryRepository = expenseCategoryRepository;
    }

    @Transactional(readOnly = true)
    public List<Budget> findAll() {
        return budgetRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Budget findById(Long id) {
        return budgetRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Budget not found"));
    }

    @Transactional
    public Budget create(BudgetUpsertRequest request) {
        ExpenseCategory expenseCategory = expenseCategoryRepository.findById(request.expenseCategoryId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense category not found"));

        Budget budget = Budget.builder()
            .expenseCategory(expenseCategory)
            .amount(request.amount())
            .initialDate(request.initialDate())
            .finalDate(request.finalDate())
            .state(BudgetState.NOT_STARTED)
            .build();

        return budgetRepository.save(budget);
    }

    @Transactional
    public Budget update(Long id, BudgetUpsertRequest request) {
        Budget budget = findById(id);

        ExpenseCategory expenseCategory = expenseCategoryRepository.findById(request.expenseCategoryId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense category not found"));

        budget.setExpenseCategory(expenseCategory);
        budget.setAmount(request.amount());
        budget.setInitialDate(request.initialDate());
        budget.setFinalDate(request.finalDate());

        return budgetRepository.save(budget);
    }

    @Transactional
    public void delete(Long id) {
        Budget budget = findById(id);
        budgetRepository.delete(budget);
    }
}
