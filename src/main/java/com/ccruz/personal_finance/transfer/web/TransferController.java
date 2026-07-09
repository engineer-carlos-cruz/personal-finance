package com.ccruz.personal_finance.transfer.web;

import com.ccruz.personal_finance.transfer.service.TransferService;
import com.ccruz.personal_finance.transfer.web.dto.TransferResponse;
import com.ccruz.personal_finance.transfer.web.dto.TransferUpsertRequest;
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
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferService transferService;
    private final TransferMapper transferMapper;

    public TransferController(TransferService transferService, TransferMapper transferMapper) {
        this.transferService = transferService;
        this.transferMapper = transferMapper;
    }

    @GetMapping
    public ResponseEntity<List<TransferResponse>> findAll() {
        return ResponseEntity.ok(transferMapper.toResponse(transferService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransferResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(transferMapper.toResponse(transferService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<TransferResponse> create(@Valid @RequestBody TransferUpsertRequest request) {
        var created = transferService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(transferMapper.toResponse(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransferResponse> update(@PathVariable Long id, @Valid @RequestBody TransferUpsertRequest request) {
        return ResponseEntity.ok(transferMapper.toResponse(transferService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        transferService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
