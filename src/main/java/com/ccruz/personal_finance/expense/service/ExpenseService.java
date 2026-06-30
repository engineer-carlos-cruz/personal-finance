package com.ccruz.personal_finance.expense.service;

import com.ccruz.personal_finance.account.persistence.Account;
import com.ccruz.personal_finance.account.persistence.AccountRepository;
import com.ccruz.personal_finance.expense.persistence.Expense;
import com.ccruz.personal_finance.expense.persistence.ExpenseRepository;
import com.ccruz.personal_finance.expense.web.dto.ExpenseUpsertRequest;
import com.ccruz.personal_finance.expense_category.persistence.ExpenseCategory;
import com.ccruz.personal_finance.expense_category.persistence.ExpenseCategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseCategoryRepository expenseCategoryRepository;
    private final AccountRepository accountRepository;

    public ExpenseService(ExpenseRepository expenseRepository, ExpenseCategoryRepository expenseCategoryRepository, AccountRepository accountRepository) {
        this.expenseRepository = expenseRepository;
        this.expenseCategoryRepository = expenseCategoryRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public List<Expense> findAll() {
        return expenseRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Expense> findAllIncludingInactive() {
        return expenseRepository.findAllIncludingInactive();
    }

    @Transactional(readOnly = true)
    public Expense findById(Long id) {
        return expenseRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found"));
    }

    @Transactional
    public Expense create(ExpenseUpsertRequest request) {
        ExpenseCategory expenseCategory = expenseCategoryRepository.findById(request.expenseCategoryId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense category not found"));

        Account account = accountRepository.findById(request.accountId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        Expense expense = Expense.builder()
            .expenseCategory(expenseCategory)
            .account(account)
            .amount(request.amount())
            .date(request.date())
            .description(request.description())
            .build();

        return expenseRepository.save(expense);
    }

    @Transactional
    public Expense update(Long id, ExpenseUpsertRequest request) {
        Expense expense = findById(id);

        ExpenseCategory expenseCategory = expenseCategoryRepository.findById(request.expenseCategoryId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense category not found"));

        Account account = accountRepository.findById(request.accountId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        expense.setExpenseCategory(expenseCategory);
        expense.setAccount(account);
        expense.setAmount(request.amount());
        expense.setDate(request.date());
        expense.setDescription(request.description());

        return expenseRepository.save(expense);
    }

    @Transactional
    public void delete(Long id) {
        Expense expense = findById(id);
        expenseRepository.delete(expense);
    }
}
