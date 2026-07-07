package com.ccruz.personal_finance.income_category.web;

import com.ccruz.personal_finance.income_category.service.IncomeCategoryService;
import com.ccruz.personal_finance.income_category.web.dto.IncomeCategoryResponse;
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
    private final IncomeCategoryMapper incomeCategoryMapper;

    public IncomeCategoryController(IncomeCategoryService incomeCategoryService, IncomeCategoryMapper incomeCategoryMapper) {
        this.incomeCategoryService = incomeCategoryService;
        this.incomeCategoryMapper = incomeCategoryMapper;
    }

    @GetMapping
    public List<IncomeCategoryResponse> findAll() {
        return incomeCategoryMapper.toResponse(incomeCategoryService.findAll());
    }

    @GetMapping("/with-inactive")
    public List<IncomeCategoryResponse> findAllIncludingInactive() {
        return incomeCategoryMapper.toResponse(incomeCategoryService.findAllIncludingInactive());
    }

    @GetMapping("/{id}")
    public IncomeCategoryResponse findById(@PathVariable Long id) {
        return incomeCategoryMapper.toResponse(incomeCategoryService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IncomeCategoryResponse create(@Valid @RequestBody IncomeCategoryUpsertRequest request) {
        return incomeCategoryMapper.toResponse(incomeCategoryService.create(request));
    }

    @PutMapping("/{id}")
    public IncomeCategoryResponse update(@PathVariable Long id, @Valid @RequestBody IncomeCategoryUpsertRequest request) {
        return incomeCategoryMapper.toResponse(incomeCategoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        incomeCategoryService.delete(id);
    }
}
