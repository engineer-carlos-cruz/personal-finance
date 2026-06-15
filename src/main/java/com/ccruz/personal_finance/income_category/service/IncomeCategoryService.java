package com.ccruz.personal_finance.income_category.service;

import com.ccruz.personal_finance.income_category.persistence.IncomeCategory;
import com.ccruz.personal_finance.income_category.persistence.IncomeCategoryRepository;
import com.ccruz.personal_finance.income_category.web.dto.IncomeCategoryUpsertRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class IncomeCategoryService {

    private final IncomeCategoryRepository incomeCategoryRepository;

    public IncomeCategoryService(IncomeCategoryRepository incomeCategoryRepository) {
        this.incomeCategoryRepository = incomeCategoryRepository;
    }

    @Transactional(readOnly = true)
    public List<IncomeCategory> findAll() {
        return incomeCategoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<IncomeCategory> findAllIncludingInactive() {
        return incomeCategoryRepository.findAllIncludingInactive();
    }

    @Transactional(readOnly = true)
    public IncomeCategory findById(Long id) {
        return incomeCategoryRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Income category not found"));
    }

    @Transactional
    public IncomeCategory create(IncomeCategoryUpsertRequest request) {
        IncomeCategory incomeCategory = IncomeCategory.builder()
            .name(request.name().trim())
            .description(request.description() == null ? null : request.description().trim())
            .build();

        return incomeCategoryRepository.save(incomeCategory);
    }

    @Transactional
    public IncomeCategory update(Long id, IncomeCategoryUpsertRequest request) {
        IncomeCategory incomeCategory = findById(id);

        incomeCategory.setName(request.name().trim());
        incomeCategory.setDescription(request.description() == null ? null : request.description().trim());

        return incomeCategoryRepository.save(incomeCategory);
    }

    @Transactional
    public void delete(Long id) {
        IncomeCategory incomeCategory = findById(id);
        incomeCategoryRepository.delete(incomeCategory);
    }
}
