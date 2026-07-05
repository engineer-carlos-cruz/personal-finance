package com.ccruz.personal_finance.budget.web;

import com.ccruz.personal_finance.budget.service.BudgetService;
import com.ccruz.personal_finance.budget.web.dto.BudgetResponse;
import com.ccruz.personal_finance.budget.web.dto.BudgetUpsertRequest;
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
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;
    private final BudgetMapper budgetMapper;

    public BudgetController(BudgetService budgetService, BudgetMapper budgetMapper) {
        this.budgetService = budgetService;
        this.budgetMapper = budgetMapper;
    }

    @GetMapping
    public List<BudgetResponse> findAll() {
        return budgetMapper.toResponse(budgetService.findAll());
    }

    @GetMapping("/with-inactive")
    public List<BudgetResponse> findAllIncludingInactive() {
        return budgetMapper.toResponse(budgetService.findAllIncludingInactive());
    }

    @GetMapping("/{id}")
    public BudgetResponse findById(@PathVariable Long id) {
        return budgetMapper.toResponse(budgetService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BudgetResponse create(@Valid @RequestBody BudgetUpsertRequest request) {
        return budgetMapper.toResponse(budgetService.create(request));
    }

    @PutMapping("/{id}")
    public BudgetResponse update(@PathVariable Long id, @Valid @RequestBody BudgetUpsertRequest request) {
        return budgetMapper.toResponse(budgetService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        budgetService.delete(id);
    }
}
