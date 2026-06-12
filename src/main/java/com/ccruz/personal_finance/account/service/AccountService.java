package com.ccruz.personal_finance.account.service;

import com.ccruz.personal_finance.account.persistence.Account;
import com.ccruz.personal_finance.account.persistence.AccountRepository;
import com.ccruz.personal_finance.account.web.dto.AccountUpsertRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Account> findAllIncludingInactive() {
        return accountRepository.findAllIncludingInactive();
    }

    @Transactional(readOnly = true)
    public Account findById(Long id) {
        return accountRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
    }

    @Transactional(readOnly = true)
    public Account findByCode(String code) {
        return accountRepository.findByCode(code)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found by code"));
    }

    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        return accountRepository.existsByCode(code);
    }

    @Transactional
    public Account create(AccountUpsertRequest request) {
        if (accountRepository.existsByCode(request.code())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Account code already exists");
        }

        Account account = Account.builder()
            .code(request.code().trim())
            .description(request.description().trim())
            .balance(request.balance())
            .build();

        return accountRepository.save(account);
    }

    @Transactional
    public Account update(Long id, AccountUpsertRequest request) {
        Account account = findById(id);

        accountRepository.findByCode(request.code())
            .filter(existing -> !existing.getId().equals(id))
            .ifPresent(existing -> {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Account code already exists");
            });

        account.setCode(request.code().trim());
        account.setDescription(request.description().trim());
        account.setBalance(request.balance());

        return accountRepository.save(account);
    }

    @Transactional
    public void delete(Long id) {
        Account account = findById(id);
        accountRepository.delete(account);
    }
}
