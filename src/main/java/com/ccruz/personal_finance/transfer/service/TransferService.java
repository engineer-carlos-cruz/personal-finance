package com.ccruz.personal_finance.transfer.service;

import com.ccruz.personal_finance.account.persistence.AccountRepository;
import com.ccruz.personal_finance.transfer.persistence.Transfer;
import com.ccruz.personal_finance.transfer.persistence.TransferRepository;
import com.ccruz.personal_finance.transfer.web.dto.TransferUpsertRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TransferService {

    private final TransferRepository transferRepository;
    private final AccountRepository accountRepository;

    public TransferService(TransferRepository transferRepository, AccountRepository accountRepository) {
        this.transferRepository = transferRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public List<Transfer> findAll() {
        return transferRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Transfer findById(Long id) {
        return transferRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer not found"));
    }

    @Transactional
    public Transfer create(TransferUpsertRequest request) {
        var sourceAccount = accountRepository.findById(request.sourceAccountId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Source account not found"));

        var targetAccount = accountRepository.findById(request.targetAccountId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Target account not found"));

        if (request.sourceAccountId().equals(request.targetAccountId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Source and target accounts must be different");
        }

        Transfer transfer = Transfer.builder()
            .sourceAccount(sourceAccount)
            .targetAccount(targetAccount)
            .amount(request.amount())
            .date(request.date())
            .build();

        return transferRepository.save(transfer);
    }

    @Transactional
    public Transfer update(Long id, TransferUpsertRequest request) {
        Transfer transfer = findById(id);

        if (request.sourceAccountId().equals(request.targetAccountId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Source and target accounts must be different");
        }

        var sourceAccount = accountRepository.findById(request.sourceAccountId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Source account not found"));

        var targetAccount = accountRepository.findById(request.targetAccountId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Target account not found"));

        transfer.setSourceAccount(sourceAccount);
        transfer.setTargetAccount(targetAccount);
        transfer.setAmount(request.amount());
        transfer.setDate(request.date());

        return transferRepository.save(transfer);
    }

    @Transactional
    public void delete(Long id) {
        Transfer transfer = findById(id);
        transferRepository.delete(transfer);
    }
}
