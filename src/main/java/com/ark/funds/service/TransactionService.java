package com.ark.funds.service;

import com.ark.funds.dto.Dtos.*;
import com.ark.funds.model.*;
import com.ark.funds.repo.*;
import com.ark.funds.web.NotFoundException;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TransactionService {
    private final TransactionRepository txs;
    private final FundRepository funds;
    private final InvestorRepository investors;

    public TransactionService(TransactionRepository txs, FundRepository funds, InvestorRepository investors) {
        this.txs = txs; this.funds = funds; this.investors = investors;
    }

    public TransactionResponse create(TransactionRequest r) {
        FundTransaction t = new FundTransaction(fund(r.fundId()), investor(r.investorId()));
        apply(t, r);
        return TransactionResponse.from(txs.save(t));
    }

    @Transactional(readOnly = true)
    public TransactionResponse get(Long id) { return TransactionResponse.from(find(id)); }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> search(Long fundId, Long investorId, TransactionType type,
                                            LocalDate from, LocalDate to, Pageable p) {
        Specification<FundTransaction> spec = (root, q, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (fundId != null) ps.add(cb.equal(root.get("fund").get("id"), fundId));
            if (investorId != null) ps.add(cb.equal(root.get("investor").get("id"), investorId));
            if (type != null) ps.add(cb.equal(root.get("type"), type));
            if (from != null) ps.add(cb.greaterThanOrEqualTo(root.<LocalDate>get("transactionDate"), from));
            if (to != null) ps.add(cb.lessThanOrEqualTo(root.<LocalDate>get("transactionDate"), to));
            return cb.and(ps.toArray(new Predicate[0]));
        };
        return txs.findAll(spec, p).map(TransactionResponse::from);
    }

    public TransactionResponse update(Long id, TransactionRequest r) {
        FundTransaction t = find(id);
        t.setFund(fund(r.fundId()));
        t.setInvestor(investor(r.investorId()));
        apply(t, r);
        return TransactionResponse.from(t);
    }

    public void delete(Long id) { txs.delete(find(id)); }

    private void apply(FundTransaction t, TransactionRequest r) {
        t.setType(r.type());
        t.setAmount(r.amount());
        t.setTransactionDate(r.transactionDate());
        t.setDescription(r.description());
    }

    private Fund fund(Long id) { return funds.findById(id).orElseThrow(() -> new NotFoundException("Fund", id)); }
    private Investor investor(Long id) { return investors.findById(id).orElseThrow(() -> new NotFoundException("Investor", id)); }
    private FundTransaction find(Long id) { return txs.findById(id).orElseThrow(() -> new NotFoundException("Transaction", id)); }
}
