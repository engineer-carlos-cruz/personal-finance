package com.ccruz.personal_finance.income.web;

import com.ccruz.personal_finance.income.persistence.Income;
import com.ccruz.personal_finance.income.service.IncomeService;
import com.ccruz.personal_finance.income.web.dto.IncomeUpsertRequest;
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
@RequestMapping("/api/incomes")
public class IncomeController {

    private final IncomeService incomeService;

    public IncomeController(IncomeService incomeService) {
        this.incomeService = incomeService;
    }

    @GetMapping
    public ResponseEntity<List<Income>> findAll() {
        return ResponseEntity.ok(incomeService.findAll());
    }

    @GetMapping("/with-inactive")
    public ResponseEntity<List<Income>> findAllIncludingInactive() {
        return ResponseEntity.ok(incomeService.findAllIncludingInactive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Income> findById(@PathVariable Long id) {
        return ResponseEntity.ok(incomeService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Income> create(@Valid @RequestBody IncomeUpsertRequest request) {
        Income created = incomeService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Income> update(@PathVariable Long id, @Valid @RequestBody IncomeUpsertRequest request) {
        return ResponseEntity.ok(incomeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        incomeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
