package com.ccruz.personal_finance.income_category.web;

import com.ccruz.personal_finance.income_category.persistence.IncomeCategory;
import com.ccruz.personal_finance.income_category.service.IncomeCategoryService;
import com.ccruz.personal_finance.income_category.web.dto.IncomeCategoryUpsertRequest;
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
@RequestMapping("/api/income-categories")
public class IncomeCategoryController {

    private final IncomeCategoryService incomeCategoryService;

    public IncomeCategoryController(IncomeCategoryService incomeCategoryService) {
        this.incomeCategoryService = incomeCategoryService;
    }

    @GetMapping
    public List<IncomeCategory> findAll() {
        return incomeCategoryService.findAll();
    }

    @GetMapping("/{id}")
    public IncomeCategory findById(@PathVariable Long id) {
        return incomeCategoryService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IncomeCategory create(@Valid @RequestBody IncomeCategoryUpsertRequest request) {
        return incomeCategoryService.create(request);
    }

    @PutMapping("/{id}")
    public IncomeCategory update(@PathVariable Long id, @Valid @RequestBody IncomeCategoryUpsertRequest request) {
        return incomeCategoryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        incomeCategoryService.delete(id);
    }
}
