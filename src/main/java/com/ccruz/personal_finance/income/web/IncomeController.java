package com.ccruz.personal_finance.income.web;

import com.ccruz.personal_finance.income.service.IncomeService;
import com.ccruz.personal_finance.income.web.dto.IncomeResponse;
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
    private final IncomeMapper incomeMapper;

    public IncomeController(IncomeService incomeService, IncomeMapper incomeMapper) {
        this.incomeService = incomeService;
        this.incomeMapper = incomeMapper;
    }

    @GetMapping
    public ResponseEntity<List<IncomeResponse>> findAll() {
        return ResponseEntity.ok(incomeMapper.toResponse(incomeService.findAll()));
    }

    @GetMapping("/with-inactive")
    public ResponseEntity<List<IncomeResponse>> findAllIncludingInactive() {
        return ResponseEntity.ok(incomeMapper.toResponse(incomeService.findAllIncludingInactive()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncomeResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(incomeMapper.toResponse(incomeService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<IncomeResponse> create(@Valid @RequestBody IncomeUpsertRequest request) {
        var created = incomeService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(incomeMapper.toResponse(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IncomeResponse> update(@PathVariable Long id, @Valid @RequestBody IncomeUpsertRequest request) {
        return ResponseEntity.ok(incomeMapper.toResponse(incomeService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        incomeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
