package com.ccruz.personal_finance.budget.web;

import com.ccruz.personal_finance.budget.persistence.Budget;
import com.ccruz.personal_finance.budget.service.BudgetService;
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

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping
    public List<Budget> findAll() {
        return budgetService.findAll();
    }

    @GetMapping("/with-inactive")
    public List<Budget> findAllIncludingInactive() {
        return budgetService.findAllIncludingInactive();
    }

    @GetMapping("/{id}")
    public Budget findById(@PathVariable Long id) {
        return budgetService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Budget create(@Valid @RequestBody BudgetUpsertRequest request) {
        return budgetService.create(request);
    }

    @PutMapping("/{id}")
    public Budget update(@PathVariable Long id, @Valid @RequestBody BudgetUpsertRequest request) {
        return budgetService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        budgetService.delete(id);
    }
}
