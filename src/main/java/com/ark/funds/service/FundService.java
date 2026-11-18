package com.ark.funds.service;

import com.ark.funds.dto.Dtos.*;
import com.ark.funds.model.Fund;
import com.ark.funds.repo.*;
import com.ark.funds.web.ConflictException;
import com.ark.funds.web.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FundService {
    private final FundRepository funds;
    private final TransactionRepository txs;
    private final InvestorRepository investors;

    public FundService(FundRepository funds, TransactionRepository txs, InvestorRepository investors) {
        this.funds = funds; this.txs = txs; this.investors = investors;
    }

    public FundResponse create(FundRequest r) {
        if (funds.existsByNameIgnoreCase(r.name().trim())) throw new ConflictException("A fund named '" + r.name() + "' already exists");
        return FundResponse.from(funds.save(new Fund(r.name().trim(), r.description())));
    }

    @Transactional(readOnly = true)
    public FundResponse get(Long id) { return FundResponse.from(find(id)); }

    @Transactional(readOnly = true)
    public Page<FundResponse> list(Pageable p) { return funds.findAll(p).map(FundResponse::from); }

    public FundResponse update(Long id, FundRequest r) {
        Fund f = find(id);
        f.setName(r.name().trim());
        f.setDescription(r.description());
        return FundResponse.from(f);
    }

    public void delete(Long id) {
        Fund f = find(id);
        if (txs.existsByFundId(id)) throw new ConflictException("Fund has transactions and cannot be deleted");
        funds.delete(f);
    }

    @Transactional(readOnly = true)
    public Page<InvestorResponse> investorsOf(Long fundId, Pageable p) {
        find(fundId);
        return investors.findByFundId(fundId, p).map(InvestorResponse::from);
    }

    private Fund find(Long id) { return funds.findById(id).orElseThrow(() -> new NotFoundException("Fund", id)); }
}
