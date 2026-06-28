package com.ccruz.personal_finance.income.service;

import com.ccruz.personal_finance.account.persistence.Account;
import com.ccruz.personal_finance.account.persistence.AccountRepository;
import com.ccruz.personal_finance.income.persistence.Income;
import com.ccruz.personal_finance.income.persistence.IncomeRepository;
import com.ccruz.personal_finance.income.web.dto.IncomeUpsertRequest;
import com.ccruz.personal_finance.income_category.persistence.IncomeCategory;
import com.ccruz.personal_finance.income_category.persistence.IncomeCategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class IncomeService {

    private final IncomeRepository incomeRepository;
    private final IncomeCategoryRepository incomeCategoryRepository;
    private final AccountRepository accountRepository;

    public IncomeService(IncomeRepository incomeRepository, IncomeCategoryRepository incomeCategoryRepository, AccountRepository accountRepository) {
        this.incomeRepository = incomeRepository;
        this.incomeCategoryRepository = incomeCategoryRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public List<Income> findAll() {
        return incomeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Income> findAllIncludingInactive() {
        return incomeRepository.findAllIncludingInactive();
    }

    @Transactional(readOnly = true)
    public Income findById(Long id) {
        return incomeRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Income not found"));
    }

    @Transactional
    public Income create(IncomeUpsertRequest request) {
        IncomeCategory incomeCategory = incomeCategoryRepository.findById(request.incomeCategoryId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Income category not found"));

        Account account = accountRepository.findById(request.accountId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        Income income = Income.builder()
            .incomeCategory(incomeCategory)
            .account(account)
            .amount(request.amount())
            .date(request.date())
            .description(request.description())
            .build();

        return incomeRepository.save(income);
    }

    @Transactional
    public Income update(Long id, IncomeUpsertRequest request) {
        Income income = findById(id);

        IncomeCategory incomeCategory = incomeCategoryRepository.findById(request.incomeCategoryId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Income category not found"));

        Account account = accountRepository.findById(request.accountId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        income.setIncomeCategory(incomeCategory);
        income.setAccount(account);
        income.setAmount(request.amount());
        income.setDate(request.date());
        income.setDescription(request.description());

        return incomeRepository.save(income);
    }

    @Transactional
    public void delete(Long id) {
        Income income = findById(id);
        incomeRepository.delete(income);
    }
}
