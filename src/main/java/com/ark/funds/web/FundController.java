package com.ark.funds.web;

import com.ark.funds.dto.Dtos.*;
import com.ark.funds.service.FundService;
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
@RequestMapping("/api/funds")
public class FundController {
    private final FundService service;
    private final ReportService reports;

    public FundController(FundService service, ReportService reports) { this.service = service; this.reports = reports; }

    @PostMapping
    public ResponseEntity<FundResponse> create(@Valid @RequestBody FundRequest r) {
        FundResponse f = service.create(r);
        return ResponseEntity.created(URI.create("/api/funds/" + f.id())).body(f);
    }

    @GetMapping
    public Page<FundResponse> list(@PageableDefault(size = 20, sort = "id") Pageable p) { return service.list(p); }

    @GetMapping("/{id}")
    public FundResponse get(@PathVariable Long id) { return service.get(id); }

    @PutMapping("/{id}")
    public FundResponse update(@PathVariable Long id, @Valid @RequestBody FundRequest r) { return service.update(id, r); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) { service.delete(id); return ResponseEntity.noContent().build(); }

    @GetMapping("/{id}/investors")
    public Page<InvestorResponse> investors(@PathVariable Long id, @PageableDefault(size = 20, sort = "id") Pageable p) {
        return service.investorsOf(id, p);
    }

    @GetMapping("/{id}/report")
    public FundReport report(@PathVariable Long id,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return reports.fundReport(id, from, to);
    }
}
