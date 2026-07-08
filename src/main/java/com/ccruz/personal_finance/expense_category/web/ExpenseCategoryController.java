package com.ccruz.personal_finance.expense_category.web;

import com.ccruz.personal_finance.expense_category.service.ExpenseCategoryService;
import com.ccruz.personal_finance.expense_category.web.dto.ExpenseCategoryResponse;
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
    private final ExpenseCategoryMapper expenseCategoryMapper;

    public ExpenseCategoryController(ExpenseCategoryService expenseCategoryService, ExpenseCategoryMapper expenseCategoryMapper) {
        this.expenseCategoryService = expenseCategoryService;
        this.expenseCategoryMapper = expenseCategoryMapper;
    }

    @GetMapping
    public List<ExpenseCategoryResponse> findAll() {
        return expenseCategoryMapper.toResponse(expenseCategoryService.findAll());
    }

    @GetMapping("/with-inactive")
    public List<ExpenseCategoryResponse> findAllIncludingInactive() {
        return expenseCategoryMapper.toResponse(expenseCategoryService.findAllIncludingInactive());
    }

    @GetMapping("/{id}")
    public ExpenseCategoryResponse findById(@PathVariable Long id) {
        return expenseCategoryMapper.toResponse(expenseCategoryService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseCategoryResponse create(@Valid @RequestBody ExpenseCategoryUpsertRequest request) {
        return expenseCategoryMapper.toResponse(expenseCategoryService.create(request));
    }

    @PutMapping("/{id}")
    public ExpenseCategoryResponse update(@PathVariable Long id, @Valid @RequestBody ExpenseCategoryUpsertRequest request) {
        return expenseCategoryMapper.toResponse(expenseCategoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        expenseCategoryService.delete(id);
    }
}
