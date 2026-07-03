package com.ccruz.personal_finance.account.web;

import com.ccruz.personal_finance.account.service.AccountService;
import com.ccruz.personal_finance.account.web.dto.AccountResponse;
import com.ccruz.personal_finance.account.web.dto.AccountUpsertRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    public AccountController(AccountService accountService, AccountMapper accountMapper) {
        this.accountService = accountService;
        this.accountMapper = accountMapper;
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> findAll() {
        return ResponseEntity.ok(accountMapper.toResponse(accountService.findAll()));
    }

    @GetMapping("/with-inactive")
    public ResponseEntity<List<AccountResponse>> findAllIncludingInactive() {
        return ResponseEntity.ok(accountMapper.toResponse(accountService.findAllIncludingInactive()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(accountMapper.toResponse(accountService.findById(id)));
    }

    @GetMapping("/by-code/{code}")
    public ResponseEntity<AccountResponse> findByCode(@PathVariable String code) {
        return ResponseEntity.ok(accountMapper.toResponse(accountService.findByCode(code)));
    }

    @GetMapping("/by-code/{code}/exists")
    public ResponseEntity<Boolean> existsByCode(@PathVariable String code) {
        return ResponseEntity.ok(accountService.existsByCode(code));
    }

    @PostMapping
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody AccountUpsertRequest request) {
        var created = accountService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(accountMapper.toResponse(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountResponse> update(@PathVariable Long id, @Valid @RequestBody AccountUpsertRequest request) {
        return ResponseEntity.ok(accountMapper.toResponse(accountService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        accountService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
