package com.ark.funds.service;

import com.ark.funds.dto.Dtos.*;
import com.ark.funds.model.TransactionType;
import com.ark.funds.repo.*;
import com.ark.funds.web.NotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Aggregates are computed in the database (grouped sums); only the small grouped result is folded in memory. */
@Service
@Transactional(readOnly = true)
public class ReportService {
    private static final LocalDate MIN = LocalDate.of(1900, 1, 1);
    private static final LocalDate MAX = LocalDate.of(9999, 12, 31);

    private final TransactionRepository txs;
    private final FundRepository funds;
    private final InvestorRepository investors;

    public ReportService(TransactionRepository txs, FundRepository funds, InvestorRepository investors) {
        this.txs = txs; this.funds = funds; this.investors = investors;
    }

    /** Totals by type, credits/debits/net, and each investor's position within the fund. */
    public FundReport fundReport(Long fundId, LocalDate from, LocalDate to) {
        var fund = funds.findById(fundId).orElseThrow(() -> new NotFoundException("Fund", fundId));
        validate(from, to);
        var rows = txs.totalsForFund(fundId, from == null ? MIN : from, to == null ? MAX : to);

        Map<TransactionType, BigDecimal> byType = emptyTypeMap();
        Map<Long, Acc> byInvestor = new LinkedHashMap<>();
        Acc total = new Acc("total");
        for (var r : rows) {
            byType.merge(r.getType(), r.getTotal(), BigDecimal::add);
            byInvestor.computeIfAbsent(r.getInvestorId(), k -> new Acc(r.getInvestorName())).add(r.getType(), r.getTotal());
            total.add(r.getType(), r.getTotal());
        }

        List<InvestorPosition> positions = byInvestor.entrySet().stream()
                .map(e -> new InvestorPosition(e.getKey(), e.getValue().name, e.getValue().credits, e.getValue().debits, e.getValue().net()))
                .sorted(Comparator.comparing(InvestorPosition::netBalance).reversed())
                .toList();
        return new FundReport(fund.getId(), fund.getName(), from, to, total.credits, total.debits, total.net(),
                positions.size(), byType, positions);
    }

    /** An investor's activity across all funds. */
    public InvestorReport investorReport(Long investorId, LocalDate from, LocalDate to) {
        var inv = investors.findById(investorId).orElseThrow(() -> new NotFoundException("Investor", investorId));
        validate(from, to);
        var rows = txs.totalsForInvestor(investorId, from == null ? MIN : from, to == null ? MAX : to);

        Map<Long, Acc> byFund = new LinkedHashMap<>();
        Map<Long, Map<TransactionType, BigDecimal>> typeByFund = new HashMap<>();
        Acc total = new Acc("total");
        for (var r : rows) {
            byFund.computeIfAbsent(r.getFundId(), k -> new Acc(r.getFundName())).add(r.getType(), r.getTotal());
            typeByFund.computeIfAbsent(r.getFundId(), k -> emptyTypeMap()).merge(r.getType(), r.getTotal(), BigDecimal::add);
            total.add(r.getType(), r.getTotal());
        }
        List<FundPosition> positions = byFund.entrySet().stream()
                .map(e -> new FundPosition(e.getKey(), e.getValue().name, e.getValue().credits, e.getValue().debits,
                        e.getValue().net(), typeByFund.get(e.getKey())))
                .toList();
        return new InvestorReport(inv.getId(), inv.getName(), from, to, total.credits, total.debits, total.net(),
                positions.size(), positions);
    }

    private static void validate(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) throw new IllegalArgumentException("'from' must not be after 'to'");
    }

    private static Map<TransactionType, BigDecimal> emptyTypeMap() {
        Map<TransactionType, BigDecimal> m = new EnumMap<>(TransactionType.class);
        for (TransactionType t : TransactionType.values()) m.put(t, BigDecimal.ZERO);
        return m;
    }

    private static final class Acc {
        final String name;
        BigDecimal credits = BigDecimal.ZERO;
        BigDecimal debits = BigDecimal.ZERO;
        Acc(String name) { this.name = name; }
        void add(TransactionType type, BigDecimal amount) {
            if (type.isCredit()) credits = credits.add(amount); else debits = debits.add(amount);
        }
        BigDecimal net() { return credits.subtract(debits); }
    }
}
