package com.ark.funds.service;

import com.ark.funds.dto.Dtos.*;
import com.ark.funds.model.Investor;
import com.ark.funds.repo.*;
import com.ark.funds.web.ConflictException;
import com.ark.funds.web.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InvestorService {
    private final InvestorRepository investors;
    private final TransactionRepository txs;
    private final FundRepository funds;

    public InvestorService(InvestorRepository investors, TransactionRepository txs, FundRepository funds) {
        this.investors = investors; this.txs = txs; this.funds = funds;
    }

    public InvestorResponse create(InvestorRequest r) {
        if (investors.existsByEmailIgnoreCase(r.email().trim())) throw new ConflictException("An investor with email '" + r.email() + "' already exists");
        return InvestorResponse.from(investors.save(new Investor(r.name().trim(), r.email().trim())));
    }

    @Transactional(readOnly = true)
    public InvestorResponse get(Long id) { return InvestorResponse.from(find(id)); }

    @Transactional(readOnly = true)
    public Page<InvestorResponse> list(Pageable p) { return investors.findAll(p).map(InvestorResponse::from); }

    public InvestorResponse update(Long id, InvestorRequest r) {
        Investor i = find(id);
        if (investors.existsByEmailIgnoreCaseAndIdNot(r.email().trim(), id)) throw new ConflictException("An investor with email '" + r.email() + "' already exists");
        i.setName(r.name().trim());
        i.setEmail(r.email().trim());
        return InvestorResponse.from(i);
    }

    public void delete(Long id) {
        Investor i = find(id);
        if (txs.existsByInvestorId(id)) throw new ConflictException("Investor has transactions and cannot be deleted");
        investors.delete(i);
    }

    @Transactional(readOnly = true)
    public Page<FundResponse> fundsOf(Long investorId, Pageable p) {
        find(investorId);
        return funds.findByInvestorId(investorId, p).map(FundResponse::from);
    }

    private Investor find(Long id) { return investors.findById(id).orElseThrow(() -> new NotFoundException("Investor", id)); }
}
