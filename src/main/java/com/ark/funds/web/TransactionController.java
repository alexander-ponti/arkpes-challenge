package com.ark.funds.web;

import com.ark.funds.dto.Dtos.*;
import com.ark.funds.model.TransactionType;
import com.ark.funds.service.TransactionService;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService service;

    public TransactionController(TransactionService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody TransactionRequest r) {
        TransactionResponse t = service.create(r);
        return ResponseEntity.created(URI.create("/api/transactions/" + t.id())).body(t);
    }

    @GetMapping
    public Page<TransactionResponse> search(
            @RequestParam(required = false) Long fundId,
            @RequestParam(required = false) Long investorId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(size = 20, sort = "transactionDate", direction = Sort.Direction.DESC) Pageable p) {
        return service.search(fundId, investorId, type, from, to, p);
    }

    @GetMapping("/{id}")
    public TransactionResponse get(@PathVariable Long id) { return service.get(id); }

    @PutMapping("/{id}")
    public TransactionResponse update(@PathVariable Long id, @Valid @RequestBody TransactionRequest r) { return service.update(id, r); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) { service.delete(id); return ResponseEntity.noContent().build(); }
}
