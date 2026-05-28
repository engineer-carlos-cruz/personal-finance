package com.ccruz.personal_finance.expense_category.web;

import com.ccruz.personal_finance.expense_category.persistence.ExpenseCategory;
import com.ccruz.personal_finance.expense_category.service.ExpenseCategoryService;
import com.ccruz.personal_finance.expense_category.web.dto.ExpenseCategoryUpsertRequest;
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
@RequestMapping("/api/expense-categories")
public class ExpenseCategoryController {

    private final ExpenseCategoryService expenseCategoryService;

    public ExpenseCategoryController(ExpenseCategoryService expenseCategoryService) {
        this.expenseCategoryService = expenseCategoryService;
    }

    @GetMapping
    public List<ExpenseCategory> findAll() {
        return expenseCategoryService.findAll();
    }

    @GetMapping("/{id}")
    public ExpenseCategory findById(@PathVariable Long id) {
        return expenseCategoryService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseCategory create(@Valid @RequestBody ExpenseCategoryUpsertRequest request) {
        return expenseCategoryService.create(request);
    }

    @PutMapping("/{id}")
    public ExpenseCategory update(@PathVariable Long id, @Valid @RequestBody ExpenseCategoryUpsertRequest request) {
        return expenseCategoryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        expenseCategoryService.delete(id);
    }
}
