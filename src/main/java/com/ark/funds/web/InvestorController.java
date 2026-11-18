package com.ark.funds.web;

import com.ark.funds.dto.Dtos.*;
import com.ark.funds.service.InvestorService;
import com.ark.funds.service.ReportService;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/investors")
public class InvestorController {
    private final InvestorService service;
    private final ReportService reports;

    public InvestorController(InvestorService service, ReportService reports) { this.service = service; this.reports = reports; }

    @PostMapping
    public ResponseEntity<InvestorResponse> create(@Valid @RequestBody InvestorRequest r) {
        InvestorResponse i = service.create(r);
        return ResponseEntity.created(URI.create("/api/investors/" + i.id())).body(i);
    }

    @GetMapping
    public Page<InvestorResponse> list(@PageableDefault(size = 20, sort = "id") Pageable p) { return service.list(p); }

    @GetMapping("/{id}")
    public InvestorResponse get(@PathVariable Long id) { return service.get(id); }

    @PutMapping("/{id}")
    public InvestorResponse update(@PathVariable Long id, @Valid @RequestBody InvestorRequest r) { return service.update(id, r); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) { service.delete(id); return ResponseEntity.noContent().build(); }

    @GetMapping("/{id}/funds")
    public Page<FundResponse> funds(@PathVariable Long id, @PageableDefault(size = 20, sort = "id") Pageable p) {
        return service.fundsOf(id, p);
    }

    @GetMapping("/{id}/report")
    public InvestorReport report(@PathVariable Long id,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return reports.investorReport(id, from, to);
    }
}
