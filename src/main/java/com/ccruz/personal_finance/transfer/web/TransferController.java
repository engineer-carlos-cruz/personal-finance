package com.ccruz.personal_finance.transfer.web;

import com.ccruz.personal_finance.transfer.persistence.Transfer;
import com.ccruz.personal_finance.transfer.service.TransferService;
import com.ccruz.personal_finance.transfer.web.dto.TransferUpsertRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @GetMapping
    public List<Transfer> findAll() {
        return transferService.findAll();
    }

    @GetMapping("/{id}")
    public Transfer findById(@PathVariable Long id) {
        return transferService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Transfer create(@Valid @RequestBody TransferUpsertRequest request) {
        return transferService.create(request);
    }

    @PutMapping("/{id}")
    public Transfer update(@PathVariable Long id, @Valid @RequestBody TransferUpsertRequest request) {
        return transferService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        transferService.delete(id);
    }
}
