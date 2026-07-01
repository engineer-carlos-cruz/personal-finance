package com.ccruz.personal_finance.expense.web;

import com.ccruz.personal_finance.account.web.dto.AccountResponse;
import com.ccruz.personal_finance.expense.persistence.Expense;
import com.ccruz.personal_finance.expense.service.ExpenseService;
import com.ccruz.personal_finance.expense.web.dto.ExpenseResponse;
import com.ccruz.personal_finance.expense.web.dto.ExpenseUpsertRequest;
import com.ccruz.personal_finance.expense_category.web.dto.ExpenseCategoryResponse;
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
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> findAll() {
        return ResponseEntity.ok(toResponse(expenseService.findAll()));
    }

    @GetMapping("/with-inactive")
    public ResponseEntity<List<ExpenseResponse>> findAllIncludingInactive() {
        return ResponseEntity.ok(toResponse(expenseService.findAllIncludingInactive()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(expenseService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> create(@Valid @RequestBody ExpenseUpsertRequest request) {
        Expense created = expenseService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(toResponse(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> update(@PathVariable Long id, @Valid @RequestBody ExpenseUpsertRequest request) {
        return ResponseEntity.ok(toResponse(expenseService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        expenseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private ExpenseResponse toResponse(Expense expense) {
        return new ExpenseResponse(
            expense.getId(),
            new ExpenseCategoryResponse(
                expense.getExpenseCategory().getId(),
                expense.getExpenseCategory().getName(),
                expense.getExpenseCategory().getDescription()
            ),
            new AccountResponse(
                expense.getAccount().getId(),
                expense.getAccount().getCode(),
                expense.getAccount().getDescription(),
                expense.getAccount().getBalance()
            ),
            expense.getAmount(),
            expense.getDate(),
            expense.getDescription()
        );
    }

    private List<ExpenseResponse> toResponse(List<Expense> expenses) {
        return expenses.stream()
                .map(this::toResponse)
                .toList();
    }
}
